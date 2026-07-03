package com.unicorn.server.infrastructure.persistence.sequence

import jakarta.persistence.EntityManager
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class SequenceGenerator(
	private val entityManager: EntityManager,
	private val dataSource: DataSource,
) {
	private val usesNextValueFor: Boolean by lazy {
		dataSource.connection.use { connection ->
			connection.metaData.databaseProductName.contains("H2", ignoreCase = true)
		}
	}

	fun nextValue(sequenceName: String): Long {
		return if (usesNextValueFor) {
			entityManager.createNativeQuery("CREATE SEQUENCE IF NOT EXISTS $sequenceName START WITH 1 INCREMENT BY 1").executeUpdate()
			(entityManager.createNativeQuery("SELECT NEXT VALUE FOR $sequenceName").singleResult as Number).toLong()
		} else {
			(entityManager.createNativeQuery("SELECT nextval('$sequenceName')").singleResult as Number).toLong()
		}
	}
}
