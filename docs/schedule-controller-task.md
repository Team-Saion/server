# Task: Schedule Controller & ApiDoc 구현

## 목표

일정(Schedule) 도메인의 web 레이어를 작성한다.
실제 비즈니스 로직은 구현하지 않는다.
Swagger UI에 6개 엔드포인트가 정상 노출되도록 컴파일 가능한 stub을 완성하는 것이 목표다.

## 참고 기준 코드 (반드시 읽고 패턴 맞출 것)

- `infrastructure/adapter/in/web/member/MemberApiDoc.kt`
- `infrastructure/adapter/in/web/member/MemberController.kt`
- `domain/member/exception/MemberErrorCode.kt`
- `infrastructure/adapter/in/web/member/dto/MemberResponse.kt`
- `infrastructure/adapter/in/web/member/dto/CompleteOnboardingRequest.kt`
- `infrastructure/adapter/in/web/common/dto/ApiResponse.kt`
- `infrastructure/adapter/in/web/common/swagger/annotation/ApiErrorCodeExample.kt`
- `infrastructure/adapter/in/web/common/swagger/annotation/ApiSuccessCodeExample.kt`

## 아키텍처 규칙

- `domain` 패키지는 Spring Web, JPA, SDK에 의존하지 않는다.
- Controller는 InPort 인터페이스만 호출한다. JPA Repository 직접 호출 금지.
- 비즈니스 로직은 Controller에 두지 않는다.
- 도메인 엔티티를 Controller에서 직접 반환하지 않는다.

---

## 생성할 파일 목록

```
src/main/kotlin/com/unicorn/server/
├── domain/schedule/
│   ├── enums/
│   │   ├── ScheduleStatus.kt
│   │   └── ConfirmationType.kt
│   ├── exception/
│   │   └── ScheduleErrorCode.kt
│   ├── port/
│   │   ├── dto/
│   │   │   ├── CreateScheduleCommand.kt
│   │   │   ├── UpdateScheduleCommand.kt
│   │   │   ├── RegisterConfirmationCommand.kt
│   │   │   ├── ScheduleSummaryResult.kt
│   │   │   ├── ScheduleListResult.kt
│   │   │   ├── ConfirmationCountResult.kt
│   │   │   └── ScheduleDetailResult.kt
│   │   └── in/
│   │       ├── CreateScheduleInPort.kt
│   │       ├── UpdateScheduleInPort.kt
│   │       ├── DeleteScheduleInPort.kt
│   │       ├── GetScheduleListInPort.kt
│   │       ├── GetScheduleDetailInPort.kt
│   │       └── RegisterConfirmationInPort.kt
│   └── service/
│       └── ScheduleService.kt
└── infrastructure/adapter/in/web/schedule/
    ├── ScheduleApiDoc.kt
    ├── ScheduleController.kt
    └── dto/
        ├── CreateScheduleRequest.kt
        ├── UpdateScheduleRequest.kt
        ├── RegisterConfirmationRequest.kt
        ├── ScheduleIdResponse.kt
        ├── ScheduleSummaryResponse.kt
        ├── ScheduleListResponse.kt
        ├── ConfirmationCountResponse.kt
        ├── ScheduleDetailResponse.kt
        └── RegisterConfirmationResponse.kt
```

---

## 1. domain/schedule/enums/ScheduleStatus.kt

```kotlin
package com.unicorn.server.domain.schedule.enums

enum class ScheduleStatus {
    UPCOMING,      // 현재 시각 < 시작일시
    IN_PROGRESS,   // 시작일시 ≤ 현재 시각 ≤ 종료일시
    COMPLETED,     // 현재 시각 > 종료일시
}
```

---

## 2. domain/schedule/enums/ConfirmationType.kt

```kotlin
package com.unicorn.server.domain.schedule.enums

// 실제 값은 추후 확정. 현재는 placeholder 단일 값.
enum class ConfirmationType {
    CONFIRMED,
}
```

---

## 3. domain/schedule/exception/ScheduleErrorCode.kt

`MemberErrorCode.kt`와 동일한 패턴. `ErrorCode` interface 구현.

```kotlin
package com.unicorn.server.domain.schedule.exception

import com.unicorn.server.common.exception.ErrorCode
import org.springframework.http.HttpStatus

enum class ScheduleErrorCode(
    override val code: String,
    override val message: String,
    override val httpStatus: HttpStatus,
) : ErrorCode {
    BLANK_TITLE("S400_1", "Title is required", HttpStatus.BAD_REQUEST),
    TITLE_TOO_LONG("S400_2", "Title must not exceed 30 characters", HttpStatus.BAD_REQUEST),
    WHITESPACE_ONLY_TITLE("S400_3", "Title must not be whitespace only", HttpStatus.BAD_REQUEST),
    MISSING_START_DATE("S400_4", "Start date is required", HttpStatus.BAD_REQUEST),
    MISSING_END_DATE("S400_5", "End date is required", HttpStatus.BAD_REQUEST),
    END_DATE_BEFORE_START_DATE("S400_6", "End date must not be before start date", HttpStatus.BAD_REQUEST),
    MISSING_START_TIME("S400_7", "Start time is required for timed schedule", HttpStatus.BAD_REQUEST),
    MISSING_END_TIME("S400_8", "End time is required for timed schedule", HttpStatus.BAD_REQUEST),
    END_TIME_NOT_AFTER_START_TIME("S400_9", "End time must be after start time", HttpStatus.BAD_REQUEST),
    MEMO_TOO_LONG("S400_10", "Memo must not exceed 500 characters", HttpStatus.BAD_REQUEST),
    CONFIRMATION_NOT_SUPPORTED("S400_11", "This schedule does not support confirmation", HttpStatus.BAD_REQUEST),
    INVALID_CONFIRMATION_TYPE("S400_12", "Invalid confirmation type", HttpStatus.BAD_REQUEST),
    CIRCLE_ACCESS_DENIED("S403_1", "No access to this circle", HttpStatus.FORBIDDEN),
    SCHEDULE_MODIFICATION_DENIED("S403_2", "Only the author or circle initiator can modify this schedule", HttpStatus.FORBIDDEN),
    CONFIRMATION_ACCESS_DENIED("S403_3", "Only circle members can register confirmation", HttpStatus.FORBIDDEN),
    CIRCLE_NOT_FOUND("S404_1", "Circle not found", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND("S404_2", "Schedule not found", HttpStatus.NOT_FOUND),
}
```

---

## 4. domain/schedule/port/dto/

### CreateScheduleCommand.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import java.time.LocalDate
import java.time.LocalTime

data class CreateScheduleCommand(
    val memberId: String,
    val circleId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val needConfirm: Boolean,
    val memo: String?,
)
```

### UpdateScheduleCommand.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import java.time.LocalDate
import java.time.LocalTime

data class UpdateScheduleCommand(
    val scheduleId: Long,
    val circleId: Long,
    val memberId: String,
    val title: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val needConfirm: Boolean,
    val memo: String?,
)
```

### RegisterConfirmationCommand.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType

data class RegisterConfirmationCommand(
    val scheduleId: Long,
    val circleId: Long,
    val memberId: String,
    val confirmationType: ConfirmationType,
)
```

### ScheduleSummaryResult.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import java.time.LocalDate
import java.time.LocalTime

data class ScheduleSummaryResult(
    val scheduleId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isAllDay: Boolean,
    val needConfirm: Boolean,
    val status: ScheduleStatus,
    val dDay: Int?,
    val progressRate: Int,
)
```

### ScheduleListResult.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

data class ScheduleListResult(
    val schedules: List<ScheduleSummaryResult>,
    val nextCursor: String?,
    val hasNext: Boolean,
)
```

### ConfirmationCountResult.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType

data class ConfirmationCountResult(
    val type: ConfirmationType,
    val count: Int,
)
```

### ScheduleDetailResult.kt

```kotlin
package com.unicorn.server.domain.schedule.port.dto

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.enums.ScheduleStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class ScheduleDetailResult(
    val scheduleId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isAllDay: Boolean,
    val needConfirm: Boolean,
    val memo: String?,
    val status: ScheduleStatus,
    val dDay: Int?,
    val progressRate: Int,
    val confirmations: List<ConfirmationCountResult>,
    val myConfirmationType: ConfirmationType?,
    val createdBy: Long,
    val createdAt: LocalDateTime,
)
```

---

## 5. domain/schedule/port/in/

각 파일은 단일 메서드 인터페이스.

### CreateScheduleInPort.kt

```kotlin
package com.unicorn.server.domain.schedule.port.`in`

import com.unicorn.server.domain.schedule.port.dto.CreateScheduleCommand

interface CreateScheduleInPort {
    fun create(command: CreateScheduleCommand): Long
}
```

### UpdateScheduleInPort.kt

```kotlin
interface UpdateScheduleInPort {
    fun update(command: UpdateScheduleCommand)
}
```

### DeleteScheduleInPort.kt

```kotlin
interface DeleteScheduleInPort {
    fun delete(scheduleId: Long, circleId: Long, memberId: String)
}
```

### GetScheduleListInPort.kt

```kotlin
interface GetScheduleListInPort {
    fun getList(
        circleId: Long,
        memberId: String,
        cursor: String?,
        size: Int,
    ): ScheduleListResult
}
```

### GetScheduleDetailInPort.kt

```kotlin
interface GetScheduleDetailInPort {
    fun getDetail(scheduleId: Long, circleId: Long, memberId: String): ScheduleDetailResult
}
```

### RegisterConfirmationInPort.kt

```kotlin
interface RegisterConfirmationInPort {
    fun register(command: RegisterConfirmationCommand): ConfirmationType
}
```

---

## 6. domain/schedule/service/ScheduleService.kt

모든 InPort를 구현하는 stub 서비스. `@Service` 부착. 모든 메서드 본문은 `throw UnsupportedOperationException("Not yet implemented")`.

```kotlin
package com.unicorn.server.domain.schedule.service

import com.unicorn.server.domain.schedule.enums.ConfirmationType
import com.unicorn.server.domain.schedule.port.dto.*
import com.unicorn.server.domain.schedule.port.`in`.*
import org.springframework.stereotype.Service

@Service
class ScheduleService :
    CreateScheduleInPort,
    UpdateScheduleInPort,
    DeleteScheduleInPort,
    GetScheduleListInPort,
    GetScheduleDetailInPort,
    RegisterConfirmationInPort {

    override fun create(command: CreateScheduleCommand): Long =
        throw UnsupportedOperationException("Not yet implemented")

    override fun update(command: UpdateScheduleCommand) =
        throw UnsupportedOperationException("Not yet implemented")

    override fun delete(scheduleId: Long, circleId: Long, memberId: String) =
        throw UnsupportedOperationException("Not yet implemented")

    override fun getList(circleId: Long, memberId: String, cursor: String?, size: Int): ScheduleListResult =
        throw UnsupportedOperationException("Not yet implemented")

    override fun getDetail(scheduleId: Long, circleId: Long, memberId: String): ScheduleDetailResult =
        throw UnsupportedOperationException("Not yet implemented")

    override fun register(command: RegisterConfirmationCommand): ConfirmationType =
        throw UnsupportedOperationException("Not yet implemented")
}
```

---

## 7. infrastructure/adapter/in/web/schedule/dto/

모든 DTO는 `@Schema` 어노테이션 포함. `MemberResponse.kt` 패턴 참고.

### CreateScheduleRequest.kt

```kotlin
package com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.LocalDate
import java.time.LocalTime

@Schema(description = "일정 생성 요청")
data class CreateScheduleRequest(
    @field:Schema(description = "일정 제목 (최대 30자)", example = "제주도 여행")
    @field:NotBlank
    @field:Size(max = 30)
    val title: String,

    @field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
    @field:NotNull
    val startDate: LocalDate,

    @field:Schema(description = "종료일 (yyyy-MM-dd). 시작일 이상이어야 한다.", example = "2024-08-03")
    @field:NotNull
    val endDate: LocalDate,

    @field:Schema(description = "시작시간 (HH:mm). 생략 시 종일 일정으로 저장된다.", example = "09:00", nullable = true)
    val startTime: LocalTime?,

    @field:Schema(description = "종료시간 (HH:mm). startTime 입력 시 필수.", example = "18:00", nullable = true)
    val endTime: LocalTime?,

    @field:Schema(description = "확인하기 기능 활성 여부", example = "true")
    @field:NotNull
    val needConfirm: Boolean,

    @field:Schema(description = "메모 (최대 500자)", example = "숙소 체크인 15시", nullable = true)
    @field:Size(max = 500)
    val memo: String?,
)
```

### UpdateScheduleRequest.kt

`CreateScheduleRequest`와 동일한 필드. 단, `title`, `startDate`, `endDate`, `startTime`, `endTime`, `memo`는 모두 nullable (선택 수정). `needConfirm`은 `Boolean` (필수, not null).

```kotlin
@Schema(description = "일정 수정 요청. 포함된 필드만 수정되며 needConfirm은 항상 포함해야 한다.")
data class UpdateScheduleRequest(
    @field:Schema(description = "수정할 제목 (최대 30자)", example = "수정된 제목", nullable = true)
    @field:Size(max = 30)
    val title: String?,

    @field:Schema(description = "수정할 시작일 (yyyy-MM-dd)", example = "2024-08-02", nullable = true)
    val startDate: LocalDate?,

    @field:Schema(description = "수정할 종료일 (yyyy-MM-dd)", example = "2024-08-04", nullable = true)
    val endDate: LocalDate?,

    @field:Schema(description = "수정할 시작시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "10:00", nullable = true)
    val startTime: LocalTime?,

    @field:Schema(description = "수정할 종료시간 (HH:mm). null 전달 시 종일 일정으로 변경.", example = "19:00", nullable = true)
    val endTime: LocalTime?,

    @field:Schema(description = "확인하기 기능 활성 여부", example = "false")
    @field:NotNull
    val needConfirm: Boolean,

    @field:Schema(description = "수정할 메모 (최대 500자)", example = "수정된 메모", nullable = true)
    @field:Size(max = 500)
    val memo: String?,
)
```

### RegisterConfirmationRequest.kt

```kotlin
@Schema(description = "확인하기 등록/변경 요청")
data class RegisterConfirmationRequest(
    @field:Schema(description = "확인하기 종류", example = "CONFIRMED")
    @field:NotNull
    val confirmationType: ConfirmationType,
)
```

### ScheduleIdResponse.kt

```kotlin
@Schema(description = "일정 생성 응답")
data class ScheduleIdResponse(
    @field:Schema(description = "생성된 일정 ID", example = "42")
    val scheduleId: Long,
) {
    companion object {
        fun of(scheduleId: Long) = ScheduleIdResponse(scheduleId)
    }
}
```

### ScheduleSummaryResponse.kt

```kotlin
@Schema(description = "일정 요약 정보")
data class ScheduleSummaryResponse(
    @field:Schema(description = "일정 ID", example = "1")
    val scheduleId: Long,

    @field:Schema(description = "일정 제목", example = "제주도 여행")
    val title: String,

    @field:Schema(description = "시작일 (yyyy-MM-dd)", example = "2024-08-01")
    val startDate: LocalDate,

    @field:Schema(description = "종료일 (yyyy-MM-dd)", example = "2024-08-03")
    val endDate: LocalDate,

    @field:Schema(description = "시작시간 (HH:mm). 종일 일정이면 null.", example = "09:00", nullable = true)
    val startTime: LocalTime?,

    @field:Schema(description = "종료시간 (HH:mm). 종일 일정이면 null.", example = "18:00", nullable = true)
    val endTime: LocalTime?,

    @field:Schema(description = "종일 일정 여부", example = "false")
    val isAllDay: Boolean,

    @field:Schema(description = "확인하기 기능 활성 여부", example = "true")
    val needConfirm: Boolean,

    @field:Schema(description = "일정 상태", example = "UPCOMING", allowableValues = ["UPCOMING", "IN_PROGRESS", "COMPLETED"])
    val status: ScheduleStatus,

    @field:Schema(description = "D-Day 값. 과거 또는 진행 중 기간 일정은 null.", example = "28", nullable = true)
    val dDay: Int?,

    @field:Schema(description = "진행률 (0~100)", example = "0")
    val progressRate: Int,
) {
    companion object {
        fun from(result: ScheduleSummaryResult) = ScheduleSummaryResponse(
            scheduleId = result.scheduleId,
            title = result.title,
            startDate = result.startDate,
            endDate = result.endDate,
            startTime = result.startTime,
            endTime = result.endTime,
            isAllDay = result.isAllDay,
            needConfirm = result.needConfirm,
            status = result.status,
            dDay = result.dDay,
            progressRate = result.progressRate,
        )
    }
}
```

### ScheduleListResponse.kt

```kotlin
@Schema(description = "일정 목록 응답")
data class ScheduleListResponse(
    @field:Schema(description = "일정 요약 목록")
    val schedules: List<ScheduleSummaryResponse>,

    @field:Schema(description = "다음 페이지 커서. hasNext=false이면 null.", nullable = true)
    val nextCursor: String?,

    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
) {
    companion object {
        fun from(result: ScheduleListResult) = ScheduleListResponse(
            schedules = result.schedules.map { ScheduleSummaryResponse.from(it) },
            nextCursor = result.nextCursor,
            hasNext = result.hasNext,
        )
    }
}
```

### ConfirmationCountResponse.kt

```kotlin
@Schema(description = "확인하기 종류별 카운트")
data class ConfirmationCountResponse(
    @field:Schema(description = "확인하기 종류", example = "CONFIRMED")
    val type: ConfirmationType,

    @field:Schema(description = "해당 종류를 선택한 멤버 수", example = "5")
    val count: Int,
) {
    companion object {
        fun from(result: ConfirmationCountResult) = ConfirmationCountResponse(type = result.type, count = result.count)
    }
}
```

### ScheduleDetailResponse.kt

```kotlin
@Schema(description = "일정 상세 응답")
data class ScheduleDetailResponse(
    // ScheduleSummaryResponse 필드 전체 포함
    val scheduleId: Long,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isAllDay: Boolean,
    val needConfirm: Boolean,
    val status: ScheduleStatus,
    val dDay: Int?,
    val progressRate: Int,

    @field:Schema(description = "메모. 없으면 null.", nullable = true)
    val memo: String?,

    @field:Schema(description = "확인하기 종류별 카운트 목록. needConfirm=false이면 빈 배열.")
    val confirmations: List<ConfirmationCountResponse>,

    @field:Schema(description = "내가 선택한 확인하기 종류. 미등록 또는 needConfirm=false이면 null.", nullable = true)
    val myConfirmationType: ConfirmationType?,

    @field:Schema(description = "작성자 memberId", example = "123")
    val createdBy: Long,

    @field:Schema(description = "생성 일시", example = "2024-07-01T10:00:00")
    val createdAt: LocalDateTime,
) {
    companion object {
        fun from(result: ScheduleDetailResult) = ScheduleDetailResponse(
            scheduleId = result.scheduleId,
            title = result.title,
            startDate = result.startDate,
            endDate = result.endDate,
            startTime = result.startTime,
            endTime = result.endTime,
            isAllDay = result.isAllDay,
            needConfirm = result.needConfirm,
            status = result.status,
            dDay = result.dDay,
            progressRate = result.progressRate,
            memo = result.memo,
            confirmations = result.confirmations.map { ConfirmationCountResponse.from(it) },
            myConfirmationType = result.myConfirmationType,
            createdBy = result.createdBy,
            createdAt = result.createdAt,
        )
    }
}
```

### RegisterConfirmationResponse.kt

```kotlin
@Schema(description = "확인하기 등록/변경 응답")
data class RegisterConfirmationResponse(
    @field:Schema(description = "최종 반영된 확인하기 종류", example = "CONFIRMED")
    val confirmationType: ConfirmationType,
) {
    companion object {
        fun of(type: ConfirmationType) = RegisterConfirmationResponse(type)
    }
}
```

---

## 8. infrastructure/adapter/in/web/schedule/ScheduleApiDoc.kt

`MemberApiDoc.kt` 패턴을 그대로 따른다.
`@Parameter(hidden = true)`를 `@AuthenticationPrincipal` 파라미터에 부착한다.

```kotlin
package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.common.exception.CommonErrorCode
import com.unicorn.server.domain.schedule.exception.ScheduleErrorCode
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiErrorCodeExamples
import com.unicorn.server.infrastructure.adapter.`in`.web.common.swagger.annotation.ApiSuccessCodeExample
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Schedule API", description = "써클 일정 생성/수정/삭제/조회 및 확인하기 API")
interface ScheduleApiDoc {

    @Operation(
        summary = "일정 생성",
        description = """
            써클에 일정을 생성합니다.

            - 제목은 1~30자, 공백 전용 불가.
            - startTime/endTime 생략 시 종일 일정으로 저장됩니다 (00:00 ~ 23:59).
            - startTime 입력 시 endTime은 필수이며, endTime은 startTime보다 이후여야 합니다.
            - needConfirm: 구성원 확인하기 기능 활성 여부.
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "BLANK_TITLE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "TITLE_TOO_LONG"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "WHITESPACE_ONLY_TITLE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_START_DATE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_DATE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MISSING_END_TIME"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
    )
    @ApiSuccessCodeExample(ScheduleIdResponse::class)
    fun createSchedule(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @RequestBody @Valid request: CreateScheduleRequest,
    ): ApiResponse<ScheduleIdResponse>

    @Operation(
        summary = "일정 수정",
        description = """
            일정을 수정합니다.

            - 작성자 또는 써클 initiator만 수정할 수 있습니다.
            - 포함된 필드만 수정됩니다 (포함되지 않은 필드는 기존 값 유지).
            - needConfirm은 항상 포함해야 합니다.
            - startTime/endTime을 null로 명시하면 종일 일정으로 변경됩니다.
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_DATE_BEFORE_START_DATE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "END_TIME_NOT_AFTER_START_TIME"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "MEMO_TOO_LONG"),
    )
    @ApiSuccessCodeExample(Unit::class)
    fun updateSchedule(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody @Valid request: UpdateScheduleRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "일정 삭제",
        description = """
            일정을 Soft Delete합니다.

            - 작성자 또는 써클 initiator만 삭제할 수 있습니다.
            - 이미 삭제된 일정은 S404_2를 반환합니다.
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_MODIFICATION_DENIED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
    )
    @ApiSuccessCodeExample(Unit::class)
    fun deleteSchedule(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
    ): ApiResponse<Unit>

    @Operation(
        summary = "일정 목록 조회",
        description = """
            써클의 일정 목록을 커서 기반 페이지네이션으로 조회합니다.

            - startDate ASC → startTime ASC 정렬 (종일 일정은 00:00 기준).
            - 삭제된 일정은 제외합니다.
            - cursor 미전달 시 처음부터 조회합니다.
            - size 기본값은 20, 최대 50입니다.
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
    )
    @ApiSuccessCodeExample(ScheduleListResponse::class)
    fun getScheduleList(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @Parameter(description = "커서. 최초 요청 시 생략.") @RequestParam cursor: String?,
        @Parameter(description = "페이지 크기. 기본값 20, 최대 50.") @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<ScheduleListResponse>

    @Operation(
        summary = "일정 상세 조회",
        description = """
            일정 상세 정보를 조회합니다.

            - 삭제된 일정은 S404_2를 반환합니다.
            - needConfirm=true인 경우 confirmations(종류별 카운트)와 myConfirmationType이 포함됩니다.
            - needConfirm=false인 경우 confirmations는 빈 배열, myConfirmationType은 null입니다.
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
    )
    @ApiSuccessCodeExample(ScheduleDetailResponse::class)
    fun getScheduleDetail(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
    ): ApiResponse<ScheduleDetailResponse>

    @Operation(
        summary = "확인하기 등록/변경",
        description = """
            일정에 확인하기를 등록하거나 종류를 변경합니다.

            - needConfirm=false인 일정은 S400_11을 반환합니다.
            - 멤버당 확인하기는 하나만 유지됩니다.
            - 기존 확인하기와 다른 종류 전달 시 새로운 종류로 변경됩니다.
            - 기존 확인하기와 동일한 종류 전달 시 그대로 유지됩니다 (멱등).
        """,
    )
    @ApiErrorCodeExamples(
        ApiErrorCodeExample(codeType = CommonErrorCode::class, code = "UNAUTHORIZED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "SCHEDULE_NOT_FOUND"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_NOT_SUPPORTED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "INVALID_CONFIRMATION_TYPE"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CIRCLE_ACCESS_DENIED"),
        ApiErrorCodeExample(codeType = ScheduleErrorCode::class, code = "CONFIRMATION_ACCESS_DENIED"),
    )
    @ApiSuccessCodeExample(RegisterConfirmationResponse::class)
    fun registerConfirmation(
        @Parameter(hidden = true) @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody @Valid request: RegisterConfirmationRequest,
    ): ApiResponse<RegisterConfirmationResponse>
}
```

---

## 9. infrastructure/adapter/in/web/schedule/ScheduleController.kt

`MemberController.kt` 패턴을 그대로 따른다.
`@RestController`, `@RequestMapping("/api/v1/circles/{circleId}/schedules")` 부착.
`ScheduleApiDoc`를 구현한다.

```kotlin
package com.unicorn.server.infrastructure.adapter.`in`.web.schedule

import com.unicorn.server.domain.schedule.port.dto.*
import com.unicorn.server.domain.schedule.port.`in`.*
import com.unicorn.server.infrastructure.adapter.`in`.web.common.dto.ApiResponse
import com.unicorn.server.infrastructure.adapter.`in`.web.schedule.dto.*
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/circles/{circleId}/schedules")
class ScheduleController(
    private val createScheduleInPort: CreateScheduleInPort,
    private val updateScheduleInPort: UpdateScheduleInPort,
    private val deleteScheduleInPort: DeleteScheduleInPort,
    private val getScheduleListInPort: GetScheduleListInPort,
    private val getScheduleDetailInPort: GetScheduleDetailInPort,
    private val registerConfirmationInPort: RegisterConfirmationInPort,
) : ScheduleApiDoc {

    @PostMapping
    override fun createSchedule(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @RequestBody @Valid request: CreateScheduleRequest,
    ): ApiResponse<ScheduleIdResponse> {
        val scheduleId = createScheduleInPort.create(
            CreateScheduleCommand(
                memberId = memberId,
                circleId = circleId,
                title = request.title,
                startDate = request.startDate,
                endDate = request.endDate,
                startTime = request.startTime,
                endTime = request.endTime,
                needConfirm = request.needConfirm,
                memo = request.memo,
            )
        )
        return ApiResponse.created(ScheduleIdResponse.of(scheduleId))
    }

    @PatchMapping("/{scheduleId}")
    override fun updateSchedule(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody @Valid request: UpdateScheduleRequest,
    ): ApiResponse<Unit> {
        updateScheduleInPort.update(
            UpdateScheduleCommand(
                scheduleId = scheduleId,
                circleId = circleId,
                memberId = memberId,
                title = request.title,
                startDate = request.startDate,
                endDate = request.endDate,
                startTime = request.startTime,
                endTime = request.endTime,
                needConfirm = request.needConfirm,
                memo = request.memo,
            )
        )
        return ApiResponse.success()
    }

    @DeleteMapping("/{scheduleId}")
    override fun deleteSchedule(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
    ): ApiResponse<Unit> {
        deleteScheduleInPort.delete(scheduleId, circleId, memberId)
        return ApiResponse.success()
    }

    @GetMapping
    override fun getScheduleList(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @RequestParam cursor: String?,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<ScheduleListResponse> {
        val result = getScheduleListInPort.getList(circleId, memberId, cursor, size)
        return ApiResponse.success(ScheduleListResponse.from(result))
    }

    @GetMapping("/{scheduleId}")
    override fun getScheduleDetail(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
    ): ApiResponse<ScheduleDetailResponse> {
        val result = getScheduleDetailInPort.getDetail(scheduleId, circleId, memberId)
        return ApiResponse.success(ScheduleDetailResponse.from(result))
    }

    @PostMapping("/{scheduleId}/confirmations")
    override fun registerConfirmation(
        @AuthenticationPrincipal memberId: String,
        @PathVariable circleId: Long,
        @PathVariable scheduleId: Long,
        @RequestBody @Valid request: RegisterConfirmationRequest,
    ): ApiResponse<RegisterConfirmationResponse> {
        val type = registerConfirmationInPort.register(
            RegisterConfirmationCommand(
                scheduleId = scheduleId,
                circleId = circleId,
                memberId = memberId,
                confirmationType = request.confirmationType,
            )
        )
        return ApiResponse.success(RegisterConfirmationResponse.of(type))
    }
}
```

---

## 10. 검증 기준

```bash
./gradlew build
```

빌드 성공 후 Swagger UI(`/swagger-ui/index.html`)에서 아래 6개 엔드포인트가 노출되는지 확인한다.

| 메서드 | 경로 |
|--------|------|
| POST | `/api/v1/circles/{circleId}/schedules` |
| PATCH | `/api/v1/circles/{circleId}/schedules/{scheduleId}` |
| DELETE | `/api/v1/circles/{circleId}/schedules/{scheduleId}` |
| GET | `/api/v1/circles/{circleId}/schedules` |
| GET | `/api/v1/circles/{circleId}/schedules/{scheduleId}` |
| POST | `/api/v1/circles/{circleId}/schedules/{scheduleId}/confirmations` |

---

## 주의사항

- `ScheduleController`의 모든 메서드 시그니처는 `ScheduleApiDoc` 인터페이스와 완전히 일치해야 한다.
- `@AuthenticationPrincipal memberId: String` 파라미터는 `ScheduleApiDoc`에서 `@Parameter(hidden = true)`로 숨긴다.
- `LocalDate`, `LocalTime` Jackson 직렬화 포맷(`yyyy-MM-dd`, `HH:mm`)이 기존 `JacksonConfig.kt`에서 처리되는지 확인한다. 없으면 추가한다.
- `ConfirmationType`은 JSON에서 대문자 enum 이름 그대로 직렬화된다.
- `domain` 패키지 내 어떤 파일도 Spring, JPA, Jackson 어노테이션을 import하지 않는다.
