package com.unicorn.server

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@Disabled("영속성 어댑터 구현 완료 후 활성화")
@SpringBootTest
@ActiveProfiles("test")
class ServerApplicationTests {

	@Test
	fun contextLoads() {
	}
}
