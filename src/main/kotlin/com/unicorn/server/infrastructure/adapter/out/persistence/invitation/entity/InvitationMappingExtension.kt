package com.unicorn.server.infrastructure.adapter.out.persistence.invitation.entity

import com.unicorn.server.domain.invitation.Invitation
import com.unicorn.server.domain.invitation.InvitationRedemption
import com.unicorn.server.domain.invitation.vo.InvitationId
import com.unicorn.server.domain.invitation.vo.InvitationRedemptionId
import com.unicorn.server.domain.invitation.vo.InvitationToken
import com.unicorn.server.domain.invitation.vo.InviteMessage
import com.unicorn.server.domain.invitation.vo.InviteToName
import com.unicorn.server.domain.member.vo.MemberId

// ========== InvitationEntity ↔ Invitation ==========

/**
 * Invitation 도메인 객체를 InvitationEntity로 변환한다.
 */
fun Invitation.toEntity(): InvitationEntity = InvitationEntity().apply {
	id = this@toEntity.id.toString()
	type = this@toEntity.type
	targetId = this@toEntity.targetId.toString()
	token = this@toEntity.token.value
	inviterId = this@toEntity.inviterId.toString()
	inviteToName = this@toEntity.inviteToName?.value?.trim()
	message = this@toEntity.message?.value?.trim()
	status = this@toEntity.status
	expiresAt = this@toEntity.expiresAt
	delYn = if (this@toEntity.deleted) "Y" else "N"
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

/**
 * InvitationEntity를 Invitation 도메인 객체로 복원한다.
 */
fun InvitationEntity.toDomain(): Invitation = Invitation(
	id = InvitationId.of(this.id),
	type = this.type,
	targetId = this.targetId,
	token = InvitationToken(this.token),
	inviterId = MemberId.of(this.inviterId),
	inviteToName = this.inviteToName?.let(::InviteToName),
	message = this.message?.let(::InviteMessage),
	status = this.status,
	expiresAt = this.expiresAt,
	deleted = this.delYn == "Y",
	createdAt = requireNotNull(this.createdAt),
	updatedAt = requireNotNull(this.updatedAt),
)

// ========== InvitationRedemptionLogEntity ↔ InvitationRedemption ==========

/**
 * InvitationRedemption 도메인 객체를 InvitationRedemptionLogEntity로 변환한다.
 */
fun InvitationRedemption.toEntity(): InvitationRedemptionLogEntity = InvitationRedemptionLogEntity().apply {
	id = this@toEntity.id.toString()
	invitationId = this@toEntity.invitationId.toString()
	redeemerMemberId = this@toEntity.redeemerMemberId.toString()
	redeemedAt = this@toEntity.redeemedAt
	createdAt = this@toEntity.createdAt
	updatedAt = this@toEntity.updatedAt
}

/**
 * InvitationRedemptionLogEntity를 InvitationRedemption 도메인 객체로 복원한다.
 */
fun InvitationRedemptionLogEntity.toDomain(): InvitationRedemption = InvitationRedemption(
	id = InvitationRedemptionId.of(this.id),
	invitationId = InvitationId.of(this.invitationId),
	redeemerMemberId = MemberId.of(this.redeemerMemberId),
	redeemedAt = this.redeemedAt,
	createdAt = requireNotNull(this.createdAt),
	updatedAt = requireNotNull(this.updatedAt),
)
