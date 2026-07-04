# 일정(Schedule) 도메인 구현 설계

---

## 1. 사전 확인 사항

### 1.1 Circle 도메인 의존성

Schedule 도메인은 Circle 도메인에 의존한다.
구체적으로는 아래 두 가지 정보가 필요하다.

| 필요 정보 | 목적 |
|-----------|------|
| 써클 존재 여부 | 일정 생성 시 써클 유효성 검증 |
| 써클 구성원 여부 | 조회/생성 권한 검증 |
| 써클 initiator 여부 | 수정/삭제 권한 검증 |

Circle 도메인이 미구현 상태라면 `CircleAccessOutPort` 구현체를 stub으로 두고 병렬 진행한다.

### 1.2 AuditableJpaEntity 패턴 보정

기존 `AuditableJpaEntity`의 `created_by` / `updated_by`는 `String?` 타입이다.
ERD DDL의 `BIGINT`와 불일치하므로 JPA 엔티티에서는 `VARCHAR` 기반 `AuditableJpaEntity`를 그대로 사용한다.

---

## 2. 전체 패키지 구조

```
src/main/kotlin/com/unicorn/server/
├── domain/schedule/
│   ├── Schedule.kt                          ← aggregate root
│   ├── ScheduleConfirmation.kt              ← entity
│   ├── enums/
│   │   ├── ScheduleStatus.kt                ← (task spec에서 생성됨)
│   │   └── ConfirmationType.kt              ← (task spec에서 생성됨)
│   ├── exception/
│   │   └── ScheduleErrorCode.kt             ← (task spec에서 생성됨)
│   ├── port/
│   │   ├── dto/                             ← (task spec에서 생성됨)
│   │   ├── in/                              ← (task spec에서 생성됨)
│   │   └── out/
│   │       ├── ScheduleOutPort.kt           ← 신규
│   │       ├── ScheduleConfirmationOutPort.kt ← 신규
│   │       └── CircleAccessOutPort.kt       ← 신규
│   └── service/
│       ├── ScheduleCommandService.kt        ← 신규 (create/update/delete)
│       ├── ScheduleQueryService.kt          ← 신규 (list/detail)
│       └── ScheduleConfirmationService.kt   ← 신규 (확인하기)
└── infrastructure/adapter/out/persistence/schedule/
    ├── entity/
    │   ├── ScheduleJpaEntity.kt             ← 신규
    │   └── ScheduleConfirmationJpaEntity.kt ← 신규
    ├── ScheduleJpaRepository.kt             ← 신규
    ├── ScheduleConfirmationJpaRepository.kt ← 신규
    ├── SchedulePersistenceAdapter.kt        ← 신규
    ├── ScheduleConfirmationPersistenceAdapter.kt ← 신규
    └── CircleAccessPersistenceAdapter.kt    ← 신규 (Circle 도메인 구현 후)
```

---

## 3. 구현 순서

### Phase 1 — 도메인 (Spring 의존 없음)

| 순서 | 파일 | 내용 |
|------|------|------|
| 1 | `Schedule.kt` | aggregate root, 비즈니스 규칙, 상태 계산 |
| 2 | `ScheduleConfirmation.kt` | 확인하기 엔티티 |
| 3 | `port/out/ScheduleOutPort.kt` | 일정 저장소 포트 |
| 4 | `port/out/ScheduleConfirmationOutPort.kt` | 확인하기 저장소 포트 |
| 5 | `port/out/CircleAccessOutPort.kt` | 써클 접근 포트 |

### Phase 2 — 유즈케이스 서비스 (@Service)

| 순서 | 파일 | 구현하는 InPort |
|------|------|----------------|
| 6 | `ScheduleCommandService.kt` | `CreateScheduleInPort`, `UpdateScheduleInPort`, `DeleteScheduleInPort` |
| 7 | `ScheduleQueryService.kt` | `GetScheduleListInPort`, `GetScheduleDetailInPort` |
| 8 | `ScheduleConfirmationService.kt` | `RegisterConfirmationInPort` |

> Phase 2 완료 후 task spec의 `ScheduleService` stub을 삭제한다.

### Phase 3 — 영속성 레이어 (@PersistenceAdapter)

| 순서 | 파일 | 내용 |
|------|------|------|
| 9 | `ScheduleJpaEntity.kt` | AuditableJpaEntity 상속, toDomain() 포함 |
| 10 | `ScheduleConfirmationJpaEntity.kt` | AuditableJpaEntity 상속, toDomain() 포함 |
| 11 | `ScheduleJpaRepository.kt` | 커서 페이지네이션 쿼리 포함 |
| 12 | `ScheduleConfirmationJpaRepository.kt` | upsert, 집계 쿼리 포함 |
| 13 | `SchedulePersistenceAdapter.kt` | ScheduleOutPort 구현 |
| 14 | `ScheduleConfirmationPersistenceAdapter.kt` | ScheduleConfirmationOutPort 구현 |
| 15 | `CircleAccessPersistenceAdapter.kt` | CircleAccessOutPort 구현 (Circle 도메인 의존) |

---

## 4. 도메인 엔티티 설계

### 4.1 Schedule.kt

```kotlin
class Schedule private constructor(
    val id: Long,               // 0 = 미저장 sentinel
    val circleId: Long,
    var title: String,
    var startDate: LocalDate,
    var endDate: LocalDate,
    var startTime: LocalTime?,  // null = 종일
    var endTime: LocalTime?,    // null = 종일
    var needConfirm: Boolean,
    var memo: String?,
    val createdBy: String,
    var updatedBy: String,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
    var isDeleted: Boolean,
) {
    val isAllDay: Boolean get() = startTime == null

    // 비즈니스 메서드: 수정
    fun update(
        title: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        startTime: LocalTime?,
        endTime: LocalTime?,
        needConfirm: Boolean,
        memo: String?,
        updatedBy: String,
    ) { ... }

    // 비즈니스 메서드: 소프트 삭제
    fun delete(deletedBy: String) { ... }

    // 계산값: 진행 상태 (저장하지 않음)
    fun computeStatus(now: LocalDateTime): ScheduleStatus { ... }

    // 계산값: D-Day (저장하지 않음)
    fun computeDDay(today: LocalDate): Int? { ... }

    // 계산값: 진행률 (저장하지 않음)
    fun computeProgressRate(now: LocalDateTime): Int { ... }

    companion object {
        // 신규 생성: 비즈니스 검증 포함
        fun create(...): Schedule { ... }

        // DB 복원: 검증 없이 재구성
        fun reconstitute(...): Schedule { ... }
    }
}
```

**`create()` 내부 검증 순서:**

| 순서 | 검증 | 예외 |
|------|------|------|
| 1 | title 공백 | `BLANK_TITLE` |
| 2 | title 공백 전용 | `WHITESPACE_ONLY_TITLE` |
| 3 | title 30자 초과 | `TITLE_TOO_LONG` |
| 4 | endDate < startDate | `END_DATE_BEFORE_START_DATE` |
| 5 | startTime만 있고 endTime 없음 | `MISSING_END_TIME` |
| 6 | endTime ≤ startTime (동일 시작일·종료일) | `END_TIME_NOT_AFTER_START_TIME` |
| 7 | memo 500자 초과 | `MEMO_TOO_LONG` |

> `update()`도 동일한 검증 적용. null 필드는 기존 값 유지.

---

### 4.2 ScheduleConfirmation.kt

```kotlin
class ScheduleConfirmation private constructor(
    val id: Long,
    val scheduleId: Long,
    val memberId: String,
    var confirmationType: ConfirmationType,
    val createdBy: String,
    var updatedBy: String,
    val createdAt: LocalDateTime,
    var updatedAt: LocalDateTime,
) {
    fun changeType(newType: ConfirmationType, updatedBy: String) { ... }

    companion object {
        fun create(...): ScheduleConfirmation { ... }
        fun reconstitute(...): ScheduleConfirmation { ... }
    }
}
```

---

## 5. 출력 포트 설계

### ScheduleOutPort.kt

```kotlin
interface ScheduleOutPort {
    fun save(schedule: Schedule): Schedule
    fun findById(scheduleId: Long): Schedule?
    fun findActiveByIdAndCircleId(scheduleId: Long, circleId: Long): Schedule?
    fun findActiveByCircleId(
        circleId: Long,
        cursor: SchedulePageCursor?,
        size: Int,
    ): List<Schedule>
}
```

### ScheduleConfirmationOutPort.kt

```kotlin
interface ScheduleConfirmationOutPort {
    fun findByScheduleIdAndMemberId(scheduleId: Long, memberId: String): ScheduleConfirmation?
    fun save(confirmation: ScheduleConfirmation): ScheduleConfirmation
    fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult>
    fun deleteAllByScheduleId(scheduleId: Long)
}
```

### CircleAccessOutPort.kt

```kotlin
interface CircleAccessOutPort {
    fun existsById(circleId: Long): Boolean
    fun isMember(circleId: Long, memberId: String): Boolean
    fun isInitiator(circleId: Long, memberId: String): Boolean
}
```

---

## 6. 유즈케이스 서비스 설계

### 6.1 ScheduleCommandService

**create 흐름:**
```
1. circleAccessOutPort.existsById(circleId)         → false: CIRCLE_NOT_FOUND
2. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
3. Schedule.create(...)                              → 도메인 검증
4. scheduleOutPort.save(schedule)
```

**update 흐름:**
```
1. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
2. scheduleOutPort.findActiveByIdAndCircleId(...)   → null: SCHEDULE_NOT_FOUND
3. isInitiator || schedule.createdBy == memberId    → false: SCHEDULE_MODIFICATION_DENIED
4. schedule.update(...)                              → 도메인 검증
5. scheduleOutPort.save(schedule)
```

**delete 흐름:**
```
1. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
2. scheduleOutPort.findActiveByIdAndCircleId(...)   → null: SCHEDULE_NOT_FOUND
3. isInitiator || schedule.createdBy == memberId    → false: SCHEDULE_MODIFICATION_DENIED
4. schedule.delete(memberId)
5. scheduleOutPort.save(schedule)
6. scheduleConfirmationOutPort.deleteAllByScheduleId(scheduleId)
```

---

### 6.2 ScheduleQueryService

**getList 흐름:**
```
1. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
2. cursor 디코딩 (없으면 null)
3. scheduleOutPort.findActiveByCircleId(circleId, cursor, size + 1)
4. hasNext = results.size > size
5. schedules = results.take(size)
6. nextCursor = if (hasNext) encode(schedules.last()) else null
7. 각 schedule에 computeStatus, computeDDay, computeProgressRate 적용
```

**getDetail 흐름:**
```
1. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
2. scheduleOutPort.findActiveByIdAndCircleId(...)   → null: SCHEDULE_NOT_FOUND
3. schedule에 computeStatus, computeDDay, computeProgressRate 적용
4. if (schedule.needConfirm):
     confirmations = scheduleConfirmationOutPort.countGroupByType(scheduleId)
     myConfirmationType = scheduleConfirmationOutPort
                            .findByScheduleIdAndMemberId(scheduleId, memberId)
                            ?.confirmationType
   else:
     confirmations = emptyList()
     myConfirmationType = null
```

---

### 6.3 ScheduleConfirmationService

**register 흐름:**
```
1. circleAccessOutPort.isMember(circleId, memberId) → false: CIRCLE_ACCESS_DENIED
2. scheduleOutPort.findActiveByIdAndCircleId(...)   → null: SCHEDULE_NOT_FOUND
3. schedule.needConfirm == false                    → CONFIRMATION_NOT_SUPPORTED
4. existing = scheduleConfirmationOutPort
               .findByScheduleIdAndMemberId(scheduleId, memberId)
5. if existing == null:
     scheduleConfirmationOutPort.save(ScheduleConfirmation.create(...))
   else if existing.confirmationType != newType:
     existing.changeType(newType, memberId)
     scheduleConfirmationOutPort.save(existing)
   // else: 동일 종류 → 멱등, 그대로 반환
6. return confirmationType
```

---

## 7. 영속성 레이어 설계

### 7.1 ScheduleJpaEntity.kt

`AuditableJpaEntity` 상속. `MemberEntity` 패턴 동일하게 적용.

```kotlin
@Entity
@Table(name = "TB_SCHEDULE")
class ScheduleJpaEntity protected constructor() : AuditableJpaEntity() {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    var id: Long = 0

    @Column(name = "circle_id", nullable = false)
    var circleId: Long = 0

    @Column(name = "title", nullable = false, length = 30)
    var title: String = ""

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate = LocalDate.now()

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate = LocalDate.now()

    @Column(name = "start_time")
    var startTime: LocalTime? = null

    @Column(name = "end_time")
    var endTime: LocalTime? = null

    @Column(name = "need_confirm", nullable = false, length = 1)
    var needConfirm: String = "N"

    @Column(name = "memo", length = 500)
    var memo: String? = null

    @Column(name = "del_yn", nullable = false, length = 1)
    var delYn: String = "N"

    constructor(schedule: Schedule) : this() { /* 필드 매핑 */ }
    fun update(schedule: Schedule) { /* 가변 필드만 갱신 */ }
    fun toDomain(): Schedule = Schedule.reconstitute(...)
}
```

### 7.2 ScheduleJpaRepository.kt

커서 페이지네이션 쿼리는 `@Query` native SQL로 작성한다.

```kotlin
interface ScheduleJpaRepository : JpaRepository<ScheduleJpaEntity, Long> {

    // 기본 단건 조회
    fun findByIdAndCircleIdAndDelYn(
        id: Long, circleId: Long, delYn: String = "N"
    ): ScheduleJpaEntity?

    // 커서 기반 목록 조회 (커서 없음 = 처음부터)
    @Query("""
        SELECT s FROM ScheduleJpaEntity s
        WHERE s.circleId = :circleId
          AND s.delYn = 'N'
        ORDER BY s.startDate ASC,
                 COALESCE(s.startTime, '00:00:00') ASC,
                 s.id ASC
    """)
    fun findFirstPage(circleId: Long, pageable: Pageable): List<ScheduleJpaEntity>

    // 커서 이후 목록 조회
    @Query(value = """
        SELECT * FROM TB_SCHEDULE
        WHERE circle_id = :circleId
          AND del_yn = 'N'
          AND (
            start_date > :cursorDate
            OR (start_date = :cursorDate
                AND COALESCE(start_time, '00:00:00') > :cursorTime)
            OR (start_date = :cursorDate
                AND COALESCE(start_time, '00:00:00') = :cursorTime
                AND schedule_id > :cursorId)
          )
        ORDER BY start_date ASC,
                 COALESCE(start_time, '00:00:00') ASC,
                 schedule_id ASC
        LIMIT :size
    """, nativeQuery = true)
    fun findAfterCursor(
        circleId: Long,
        cursorDate: LocalDate,
        cursorTime: LocalTime,
        cursorId: Long,
        size: Int,
    ): List<ScheduleJpaEntity>
}
```

### 7.3 ScheduleConfirmationJpaRepository.kt

```kotlin
interface ScheduleConfirmationJpaRepository
    : JpaRepository<ScheduleConfirmationJpaEntity, Long> {

    fun findByScheduleIdAndMemberId(
        scheduleId: Long, memberId: String
    ): ScheduleConfirmationJpaEntity?

    // ConfirmationType별 count 집계
    @Query("""
        SELECT new com.unicorn.server.domain.schedule.port.dto.ConfirmationCountResult(
            c.confirmationType, COUNT(c)
        )
        FROM ScheduleConfirmationJpaEntity c
        WHERE c.scheduleId = :scheduleId
        GROUP BY c.confirmationType
    """)
    fun countGroupByType(scheduleId: Long): List<ConfirmationCountResult>

    fun deleteAllByScheduleId(scheduleId: Long)
}
```

---

## 8. 핵심 계산 로직

### 8.1 상태 계산 (Schedule.computeStatus)

```kotlin
fun computeStatus(now: LocalDateTime): ScheduleStatus {
    val startDt = LocalDateTime.of(startDate, startTime ?: LocalTime.of(0, 0))
    val endDt   = LocalDateTime.of(endDate,   endTime   ?: LocalTime.of(23, 59))
    return when {
        now.isBefore(startDt) -> ScheduleStatus.UPCOMING
        now.isAfter(endDt)    -> ScheduleStatus.COMPLETED
        else                  -> ScheduleStatus.IN_PROGRESS
    }
}
```

### 8.2 D-Day 계산 (Schedule.computeDDay)

```kotlin
fun computeDDay(today: LocalDate): Int? {
    // 종료일이 과거면 미표시
    if (endDate.isBefore(today)) return null
    // 시작일이 과거이고 종료일이 오늘 이후면 진행 중 → 미표시
    if (startDate.isBefore(today)) return null
    // 오늘 시작이면 0 (D-Day), 미래면 양수
    return ChronoUnit.DAYS.between(today, startDate).toInt()
}
```

### 8.3 진행률 계산 (Schedule.computeProgressRate)

```kotlin
fun computeProgressRate(now: LocalDateTime): Int {
    val startDt = LocalDateTime.of(startDate, startTime ?: LocalTime.of(0, 0))
    val endDt   = LocalDateTime.of(endDate,   endTime   ?: LocalTime.of(23, 59))
    return when {
        now.isBefore(startDt) -> 0
        now.isAfter(endDt)    -> 100
        else -> {
            val total   = ChronoUnit.SECONDS.between(startDt, endDt)
            val elapsed = ChronoUnit.SECONDS.between(startDt, now)
            if (total == 0L) 100
            else ((elapsed.toDouble() / total) * 100).toInt().coerceIn(0, 100)
        }
    }
}
```

### 8.4 커서 인코딩

커서는 `(startDate, startTime, scheduleId)` 세 값을 JSON 직렬화 후 Base64 URL-safe 인코딩한다.

```kotlin
// domain/schedule/port/dto/SchedulePageCursor.kt
data class SchedulePageCursor(
    val startDate: LocalDate,
    val startTime: LocalTime?,   // null이면 00:00으로 취급
    val scheduleId: Long,
) {
    fun encode(): String { /* JSON → Base64 */ }

    companion object {
        fun decode(cursor: String): SchedulePageCursor { /* Base64 → JSON */ }
        fun from(schedule: Schedule) = SchedulePageCursor(
            startDate   = schedule.startDate,
            startTime   = schedule.startTime,
            scheduleId  = schedule.id,
        )
    }
}
```

---

## 9. 의존 방향 요약

```
ScheduleController
    → ScheduleCommandService    (CreateScheduleInPort 등)
    → ScheduleQueryService      (GetScheduleListInPort 등)
    → ScheduleConfirmationService (RegisterConfirmationInPort)

ScheduleCommandService
    → ScheduleOutPort           ← SchedulePersistenceAdapter
    → ScheduleConfirmationOutPort ← ScheduleConfirmationPersistenceAdapter
    → CircleAccessOutPort       ← CircleAccessPersistenceAdapter

ScheduleQueryService
    → ScheduleOutPort
    → ScheduleConfirmationOutPort
    → CircleAccessOutPort

ScheduleConfirmationService
    → ScheduleOutPort
    → ScheduleConfirmationOutPort
    → CircleAccessOutPort
```

---

## 10. 미결 사항

| 항목 | 현재 결정 |
|------|----------|
| Circle 도메인 구현 상태 | `CircleAccessOutPort` stub으로 우선 진행 |
| 커서 페이지네이션 Native SQL | PostgreSQL 기준 작성, H2 테스트 환경 호환성 확인 필요 |
| `ScheduleConfirmationJpaRepository.countGroupByType` JPQL | `ConfirmationCountResult`가 domain 패키지에 있어 JPQL new 표현식 사용 가능한지 확인 필요. 불가하면 Tuple 또는 interface projection으로 대체 |
| `ConfirmationType` 실제 값 | placeholder `CONFIRMED` → 확정 후 enum 추가 |
| KST 기준 now 주입 | `LocalDateTime.now(ZoneId.of("Asia/Seoul"))` 또는 `Clock` bean 주입으로 통일 |
