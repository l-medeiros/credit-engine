package com.lucasmedeiros.creditengine

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CreditEngineApplicationTests {

	@Test
	fun contextLoads() {
		return Unit
	}
}
