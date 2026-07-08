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
		val sql = if (usesNextValueFor) {
			"SELECT NEXT VALUE FOR $sequenceName"
		} else {
			"SELECT nextval('$sequenceName')"
		}
		return (entityManager.createNativeQuery(sql).singleResult as Number).toLong()
	}
}
