package com.unicorn.server.infrastructure.adapter.out.persistence.notification

import com.unicorn.server.infrastructure.adapter.out.persistence.notification.entity.NotificationSettingEntity
import org.springframework.data.jpa.repository.JpaRepository

interface NotificationSettingJpaRepository : JpaRepository<NotificationSettingEntity, String>
