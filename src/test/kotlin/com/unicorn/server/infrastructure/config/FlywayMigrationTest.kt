package com.unicorn.server.infrastructure.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(
	properties = [
		"spring.flyway.enabled=true",
		"spring.flyway.locations=classpath:db/migration",
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.datasource.url=jdbc:h2:mem:flyway-verify;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false",
	],
)
@ActiveProfiles("test")
@DisplayName("Flyway 마이그레이션 검증 테스트")
class FlywayMigrationTest(
	@param:Autowired private val jdbcTemplate: JdbcTemplate,
) {

	@Test
	@DisplayName("모든 마이그레이션이 성공적으로 적용된다")
	fun migrate_allMigrations_appliedSuccessfully() {
		val appliedCount = jdbcTemplate.queryForObject(
			"""select count(*) from "flyway_schema_history" where "success" = true""",
			Long::class.java,
		)

		assertThat(appliedCount).isGreaterThanOrEqualTo(11)
	}

	@Test
	@DisplayName("일정 테이블에 써클 FK 제약이 존재한다")
	fun migrate_scheduleTable_hasCircleForeignKey() {
		val fkCount = jdbcTemplate.queryForObject(
			"""
				select count(*)
				from information_schema.table_constraints
				where upper(constraint_name) = 'FK_SCHEDULE_CIRCLE'
					and constraint_type = 'FOREIGN KEY'
			""",
			Long::class.java,
		)

		assertThat(fkCount).isEqualTo(1L)
	}
}
