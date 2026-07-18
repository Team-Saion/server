package com.unicorn.server.domain.circle.exception

class CircleSuccessorNotFoundException(circleId: String) :
	RuntimeException("Active circle successor not found: circleId=$circleId")
