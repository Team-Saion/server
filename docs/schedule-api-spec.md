    # 일정(Schedule) API 명세서

> Version: MVP
> Base URL: `/api/v1`
> 인증: Bearer Token (Authorization 헤더)
> 타임존: KST 고정

---

## 목차

1. [ERD 보정 사항](#1-erd-보정-사항)
2. [공통 응답 형식](#2-공통-응답-형식)
3. [에러 코드](#3-에러-코드)
4. [공통 도메인 모델](#4-공통-도메인-모델)
5. [API 목록](#5-api-목록)
6. [일정 생성](#6-일정-생성)
7. [일정 수정](#7-일정-수정)
8. [일정 삭제](#8-일정-삭제)
9. [일정 목록 조회](#9-일정-목록-조회)
10. [일정 상세 조회](#10-일정-상세-조회)
11. [확인하기 등록/변경](#11-확인하기-등록변경)

---

## 1. ERD 보정 사항

기존 ERD와 정책 문서 간 불일치 항목을 정리한다.

| 항목 | 기존 ERD | 정책 기준 보정 | 사유 |
|------|----------|---------------|------|
| `schedule_date` | DATE 단일 컬럼 | `start_date DATE` + `end_date DATE`로 분리 | 정책: 시작일·종료일 모두 필수 |
| `schedule_time` | TIME 단일 컬럼 | `start_time TIME NULL` + `end_time TIME NULL`로 분리 | 정책: 시간 일정에서 종료시간 필수 |
| `memo` | VARCHAR(200) | VARCHAR(500) | 정책: 메모 최대 500자 |
| `del_yn` 타입 | CHAR(1) | CHAR(1) `Y`/`N` 유지 | 기존 패턴 동일 |

### 보정된 TB_SCHEDULE

```sql
CREATE TABLE TB_SCHEDULE (
    schedule_id  BIGINT       NOT NULL AUTO_INCREMENT,
    circle_id    BIGINT       NOT NULL,
    title        VARCHAR(30)  NOT NULL,
    start_date   DATE         NOT NULL,
    end_date     DATE         NOT NULL,
    start_time   TIME         NULL,         -- NULL = 종일 일정
    end_time     TIME         NULL,         -- NULL = 종일 일정
    need_confirm CHAR(1)      NOT NULL DEFAULT 'N',
    memo         VARCHAR(500) NULL,
    created_at   DATETIME     NOT NULL,
    updated_at   DATETIME     NOT NULL,
    created_by   BIGINT       NOT NULL,
    updated_by   BIGINT       NOT NULL,
    del_yn       CHAR(1)      NOT NULL DEFAULT 'N',
    PRIMARY KEY (schedule_id),
    CONSTRAINT fk_schedule_circle FOREIGN KEY (circle_id) REFERENCES TB_CIRCLE (circle_id)
);
```

> `TB_SCHEDULE_RESPONSE`는 MVP에서 참여 상태가 제외되어 생성하지 않는다.

### TB_SCHEDULE_CONFIRMATION

```sql
CREATE TABLE TB_SCHEDULE_CONFIRMATION (
    confirmation_id   BIGINT      NOT NULL AUTO_INCREMENT,
    schedule_id       BIGINT      NOT NULL,
    member_id         BIGINT      NOT NULL,
    confirmation_type VARCHAR(30) NOT NULL,
    created_at        DATETIME    NOT NULL,
    updated_at        DATETIME    NOT NULL,
    created_by        BIGINT      NOT NULL,
    updated_by        BIGINT      NOT NULL,
    PRIMARY KEY (confirmation_id),
    UNIQUE KEY uq_schedule_member (schedule_id, member_id),
    CONSTRAINT fk_confirmation_schedule FOREIGN KEY (schedule_id) REFERENCES TB_SCHEDULE (schedule_id)
);
```

- 일정 당 멤버는 확인하기를 1건만 가질 수 있다.
- 다른 종류로 재등록 시 기존 Row를 UPDATE한다.
- `del_yn` 없음 — 취소가 필요한 경우 Row를 DELETE한다.

---

## 2. 공통 응답 형식

모든 API는 아래 래퍼로 응답한다.

### 성공 응답

```json
{
  "success": true,
  "data": { },
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

### 실패 응답

```json
{
  "success": false,
  "data": null,
  "errorCode": "S400_1",
  "message": "제목은 필수 입력 값입니다.",
  "timestamp": "2024-08-01T10:00:00"
}
```

---

## 3. 에러 코드

### 공통 에러 코드

| 코드 | HTTP | 의미 |
|------|------|------|
| `G401` | 401 | 인증 필요 |
| `G403` | 403 | 접근 거부 |
| `G404` | 404 | 리소스 없음 |
| `G400` | 400 | 잘못된 입력 |
| `G500` | 500 | 서버 오류 |

### 일정 도메인 에러 코드

| 코드 | HTTP | 의미 |
|------|------|------|
| `S400_1` | 400 | 제목은 필수 입력 값입니다. |
| `S400_2` | 400 | 제목은 최대 30자까지 입력할 수 있습니다. |
| `S400_3` | 400 | 제목에 공백만 입력할 수 없습니다. |
| `S400_4` | 400 | 시작일은 필수 입력 값입니다. |
| `S400_5` | 400 | 종료일은 필수 입력 값입니다. |
| `S400_6` | 400 | 종료일은 시작일보다 빠를 수 없습니다. |
| `S400_7` | 400 | 시간 일정에서 시작시간은 필수 입력 값입니다. |
| `S400_8` | 400 | 시간 일정에서 종료시간은 필수 입력 값입니다. |
| `S400_9` | 400 | 종료시간은 시작시간보다 이후여야 합니다. |
| `S400_10` | 400 | 메모는 최대 500자까지 입력할 수 있습니다. |
| `S403_1` | 403 | 해당 써클에 접근 권한이 없습니다. |
| `S403_2` | 403 | 일정 수정/삭제 권한이 없습니다. (작성자 또는 써클 권한 보유자만 가능) |
| `S403_3` | 403 | 써클 구성원만 확인하기를 등록할 수 있습니다. |
| `S404_1` | 404 | 써클을 찾을 수 없습니다. |
| `S404_2` | 404 | 일정을 찾을 수 없습니다. |
| `S400_11` | 400 | 확인하기를 지원하지 않는 일정입니다. (needConfirm=false) |
| `S400_12` | 400 | 유효하지 않은 확인하기 종류입니다. |

---

## 4. 공통 도메인 모델

### ScheduleStatus

| 값 | 의미 | 조건 |
|----|------|------|
| `UPCOMING` | 예정 | 현재 시각 < 시작일시 |
| `IN_PROGRESS` | 진행 중 | 시작일시 ≤ 현재 시각 ≤ 종료일시 |
| `COMPLETED` | 종료 | 현재 시각 > 종료일시 |

> 상태는 저장하지 않고 조회 시 KST 기준으로 계산한다.
> 종일 일정의 시작일시 = `start_date 00:00`, 종료일시 = `end_date 23:59`.

### dDay 규칙

| 조건 | 반환값 | 화면 표시 |
|------|--------|----------|
| 시작일 > 오늘 | 양수 N | `D-N` |
| 시작일 = 오늘 | `0` | `D-Day` |
| 시작일 < 오늘이지만 종료일 ≥ 오늘 (기간 진행 중) | `null` | 클라이언트에서 진행 중 상태 우선 표시 |
| 종료일 < 오늘 (완료) | `null` | D-Day 미표시 |

### progressRate 규칙

| 상태 | 값 |
|------|----|
| UPCOMING | `0` |
| IN_PROGRESS | `(경과 시간 / 전체 기간) × 100` (정수, 0~100) |
| COMPLETED | `100` |

### ConfirmationType

확인하기 종류는 서버 enum으로 관리한다. 실제 값은 별도 정의한다.

> 예시: `CONFIRMED`, `WILL_ATTEND`, `CANNOT_ATTEND` 등 (확정 전)

### ScheduleSummary (목록 공통 응답 모델)

```json
{
  "scheduleId": 1,
  "title": "제주도 여행",
  "startDate": "2024-08-01",
  "endDate": "2024-08-03",
  "startTime": "09:00",
  "endTime": "18:00",
  "isAllDay": false,
  "needConfirm": true,
  "status": "UPCOMING",
  "dDay": 28,
  "progressRate": 0
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `scheduleId` | Long | 일정 식별자 |
| `title` | String | 제목 |
| `startDate` | String (yyyy-MM-dd) | 시작일 |
| `endDate` | String (yyyy-MM-dd) | 종료일 |
| `startTime` | String? (HH:mm) | 시작시간. 종일 일정이면 null |
| `endTime` | String? (HH:mm) | 종료시간. 종일 일정이면 null |
| `isAllDay` | Boolean | 종일 일정 여부 |
| `needConfirm` | Boolean | 확인하기 기능 활성 여부 |
| `status` | String | UPCOMING / IN_PROGRESS / COMPLETED |
| `dDay` | Int? | D-Day 값. null이면 미표시 |
| `progressRate` | Int | 진행률 (0~100) |

---

## 5. API 목록

| # | 메서드 | 경로 | 설명 |
|---|--------|------|------|
| 1 | POST | `/api/v1/circles/{circleId}/schedules` | 일정 생성 |
| 2 | PATCH | `/api/v1/circles/{circleId}/schedules/{scheduleId}` | 일정 수정 |
| 3 | DELETE | `/api/v1/circles/{circleId}/schedules/{scheduleId}` | 일정 삭제 |
| 4 | GET | `/api/v1/circles/{circleId}/schedules` | 일정 목록 조회 |
| 5 | GET | `/api/v1/circles/{circleId}/schedules/{scheduleId}` | 일정 상세 조회 |
| 6 | POST | `/api/v1/circles/{circleId}/schedules/{scheduleId}/confirmations` | 확인하기 등록/변경 |

---

## 6. 일정 생성

### `POST /api/v1/circles/{circleId}/schedules`

**권한:** 써클 구성원

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 대상 써클 ID |

### Request Body

```json
{
  "title": "제주도 여행",
  "startDate": "2024-08-01",
  "endDate": "2024-08-03",
  "startTime": "09:00",
  "endTime": "18:00",
  "needConfirm": true,
  "memo": "숙소 체크인 15시"
}
```

| 필드          | 타입                  | 필수 | 제약 |
|-------------|---------------------|------|------|
| `title`     | String              | 필수 | 1~30자, 공백 전용 불가 |
| `startDate` | String (yyyy-MM-dd) | 필수 | 과거 허용 |
| `endDate`   | String (yyyy-MM-dd) | 필수 | startDate 이상 |
| `startTime` | String? (HH:mm)     | 선택 | 생략 시 종일 일정 (`00:00` 저장) |
| `endTime`   | String? (HH:mm)     | 조건부 | startTime 입력 시 필수, startTime 이후 |
| `needConfirm` | Boolean           | 필수 | 확인 응답 필요 여부 |
| `memo`      | String?             | 선택 | 최대 500자 |

> startTime/endTime 모두 생략 → 종일 일정 (`start_time=00:00`, `end_time=23:59` 저장)
> startTime만 있고 endTime 없음 → `S400_8` 오류

### Response `201 Created`

```json
{
  "success": true,
  "data": {
    "scheduleId": 42
  },
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| 제목 없음 또는 공백 | `S400_1` / `S400_3` | 400 |
| 제목 30자 초과 | `S400_2` | 400 |
| 시작일 없음 | `S400_4` | 400 |
| 종료일 없음 | `S400_5` | 400 |
| 종료일 < 시작일 | `S400_6` | 400 |
| startTime 있고 endTime 없음 | `S400_8` | 400 |
| endTime ≤ startTime (같은 날) | `S400_9` | 400 |
| 메모 500자 초과 | `S400_10` | 400 |
| 써클 없음 | `S404_1` | 404 |
| 써클 구성원 아님 | `S403_1` | 403 |

---

## 7. 일정 수정

### `PATCH /api/v1/circles/{circleId}/schedules/{scheduleId}`

**권한:** 작성자 또는 써클 initiator

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 써클 ID |
| `scheduleId` | Long | 일정 ID |

### Request Body

수정할 필드만 포함한다. 포함되지 않은 필드는 기존 값을 유지한다.

```json
{
  "title": "수정된 제목",
  "startDate": "2024-08-02",
  "endDate": "2024-08-04",
  "startTime": null,
  "endTime": null,
  "needConfirm": false,
  "memo": "수정된 메모"
}
```

> `startTime: null` / `endTime: null`을 명시적으로 전달하면 종일 일정으로 변경한다.

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `title` | String? | 선택 | 생성과 동일한 검증 |
| `startDate` | String? (yyyy-MM-dd) | 선택 | |
| `endDate` | String? (yyyy-MM-dd) | 선택 | startDate 이상 |
| `startTime` | String? (HH:mm) | 선택 | null 허용 |
| `endTime` | String? (HH:mm) | 선택 | startTime 있으면 필수 |
| `needConfirm` | Boolean | 필수 | 확인 응답 필요 여부 |
| `memo` | String? | 선택 | 최대 500자 |

### Response `200 OK`

```json
{
  "success": true,
  "data": null,
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| 일정 없음 (삭제 포함) | `S404_2` | 404 |
| 수정 권한 없음 | `S403_2` | 403 |
| 써클 접근 권한 없음 | `S403_1` | 403 |
| 입력값 검증 오류 | 생성과 동일 | 400 |

---

## 8. 일정 삭제

### `DELETE /api/v1/circles/{circleId}/schedules/{scheduleId}`

**권한:** 작성자 또는 써클 initiator
**방식:** Soft Delete (`del_yn = 'Y'`)

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 써클 ID |
| `scheduleId` | Long | 일정 ID |

### Response `200 OK`

```json
{
  "success": true,
  "data": null,
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| 일정 없음 | `S404_2` | 404 |
| 이미 삭제된 일정 | `S404_2` | 404 |
| 삭제 권한 없음 | `S403_2` | 403 |
| 써클 접근 권한 없음 | `S403_1` | 403 |

> 이미 삭제된 일정 재삭제는 `S404_2`로 처리한다. (멱등 처리 대신 실패 처리)

---

## 9. 일정 목록 조회

### `GET /api/v1/circles/{circleId}/schedules`

**권한:** 써클 구성원
**정렬:** startDate ASC → startTime ASC (종일 일정은 00:00 기준)
**페이지네이션:** 커서 기반

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 써클 ID |

### Query Parameters

| 파라미터 | 타입 | 기본값 | 설명 |
|----------|------|--------|------|
| `cursor` | String? | — | 이전 응답의 `nextCursor`. 최초 요청 시 생략 |
| `size` | Int? | `20` | 페이지 크기 (최대 50) |

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "schedules": [
      {
        "scheduleId": 1,
        "title": "제주도 여행",
        "startDate": "2024-08-01",
        "endDate": "2024-08-03",
        "startTime": "09:00",
        "endTime": "18:00",
        "isAllDay": false,
        "status": "UPCOMING",
        "dDay": 28,
        "progressRate": 0
      },
      {
        "scheduleId": 2,
        "title": "팀 워크숍",
        "startDate": "2024-08-10",
        "endDate": "2024-08-10",
        "startTime": null,
        "endTime": null,
        "isAllDay": true,
        "status": "UPCOMING",
        "dDay": 37,
        "progressRate": 0
      }
    ],
    "nextCursor": "eyJzdGFydERhdGUiOiIyMDI0LTA4LTEwIiwic2NoZWR1bGVJZCI6Mn0=",
    "hasNext": true
  },
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `schedules` | Array | ScheduleSummary 목록 |
| `nextCursor` | String? | 다음 페이지 커서. `hasNext=false`면 null |
| `hasNext` | Boolean | 다음 페이지 존재 여부 |

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| 써클 없음 | `S404_1` | 404 |
| 써클 접근 권한 없음 | `S403_1` | 403 |

---

## 10. 일정 상세 조회

### `GET /api/v1/circles/{circleId}/schedules/{scheduleId}`

**권한:** 써클 구성원

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 써클 ID |
| `scheduleId` | Long | 일정 ID |

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "scheduleId": 1,
    "title": "제주도 여행",
    "startDate": "2024-08-01",
    "endDate": "2024-08-03",
    "startTime": "09:00",
    "endTime": "18:00",
    "isAllDay": false,
    "needConfirm": true,
    "memo": "숙소 체크인 15시",
    "status": "UPCOMING",
    "dDay": 28,
    "progressRate": 0,
    "confirmations": [
      { "type": "CONFIRMED", "count": 5 },
      { "type": "CANNOT_ATTEND", "count": 1 }
    ],
    "myConfirmationType": "CONFIRMED",
    "createdBy": 123,
    "createdAt": "2024-07-01T10:00:00"
  },
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| (ScheduleSummary 필드 전체 포함) | | |
| `memo` | String? | 메모. 없으면 null |
| `confirmations` | Array | 확인하기 종류별 카운트 목록. `needConfirm=false`이면 빈 배열 |
| `confirmations[].type` | String | 확인하기 종류 (ConfirmationType enum) |
| `confirmations[].count` | Int | 해당 종류를 선택한 멤버 수 |
| `myConfirmationType` | String? | 내가 선택한 확인하기 종류. 미등록이면 null. `needConfirm=false`이면 null |
| `createdBy` | Long | 작성자 memberId |
| `createdAt` | String (ISO 8601) | 생성 일시 |

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| 일정 없음 또는 삭제됨 | `S404_2` | 404 |
| 써클 접근 권한 없음 | `S403_1` | 403 |

---

## 11. 확인하기 등록/변경

### `POST /api/v1/circles/{circleId}/schedules/{scheduleId}/confirmations`

**권한:** 써클 구성원
**동작:** 기존 확인하기 없으면 생성, 있으면 새로운 종류로 변경 (Upsert)

> `needConfirm=false`인 일정에 요청하면 `S400_11` 반환.

### Path Parameters

| 파라미터 | 타입 | 설명 |
|----------|------|------|
| `circleId` | Long | 써클 ID |
| `scheduleId` | Long | 일정 ID |

### Request Body

```json
{
  "confirmationType": "CONFIRMED"
}
```

| 필드 | 타입 | 필수 | 제약 |
|------|------|------|------|
| `confirmationType` | String | 필수 | ConfirmationType enum 값 |

### 동작 규칙

| 현재 상태 | 요청 종류 | 결과 |
|----------|----------|------|
| 확인하기 없음 | 임의 종류 | 신규 생성 |
| 확인하기 있음 (동일 종류) | 동일 종류 | 그대로 유지 (멱등) |
| 확인하기 있음 (다른 종류) | 다른 종류 | 기존 종류 → 새로운 종류로 변경 |

### Response `200 OK`

```json
{
  "success": true,
  "data": {
    "confirmationType": "CONFIRMED"
  },
  "errorCode": null,
  "message": null,
  "timestamp": "2024-08-01T10:00:00"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `confirmationType` | String | 최종 반영된 확인하기 종류 |

### 오류 응답

| 상황 | 에러 코드 | HTTP |
|------|----------|------|
| needConfirm=false인 일정 | `S400_11` | 400 |
| 유효하지 않은 confirmationType | `S400_12` | 400 |
| 일정 없음 또는 삭제됨 | `S404_2` | 404 |
| 써클 접근 권한 없음 | `S403_1` | 403 |

---

## 부록: 미결 확인 필요 항목

정책서의 미결 사항 중 API 설계에 직접 영향을 주는 항목이다.

| 항목 | 현재 처리 방식 | 확인 필요 내용 |
|------|---------------|---------------|
| 삭제된 일정 재삭제 | `S404_2` 반환 (실패 처리) | 멱등 처리(200 반환)로 변경할지 확인 필요 |
| 삭제된 일정 상세 조회 | `S404_2` 반환 | 삭제 상태 응답으로 변경할지 확인 필요 |
| 써클 권한 체계 | `initiator` 기준 | 써클 권한 보유자 구체적 정의 확인 필요 |
