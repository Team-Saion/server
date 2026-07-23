package com.unicorn.server.domain.schedule.enums

// 긴급도가 높은 레벨부터 선언한다. maxDayThreshold는 "dDay < maxDayThreshold"를 만족할 때 해당 레벨로 판단하며,
// null이면 상한이 없는 catch-all 레벨을 의미한다. 기준 변경/레벨 추가는 이 enum 수정만으로 반영된다.
enum class UrgencyLevel(val maxDayThreshold: Int?) {
	URGENT(10),
	NORMAL(null),
	;

	companion object {
		fun from(dDay: Int?): UrgencyLevel {
			if (dDay == null) return NORMAL
			return entries.firstOrNull { it.maxDayThreshold != null && dDay < it.maxDayThreshold }
				?: NORMAL
		}
	}
}
