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
	CONFIRMATION_NOT_FOUND("S404_3", "Confirmation not found", HttpStatus.NOT_FOUND),
}
