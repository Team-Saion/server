package com.unicorn.server.infrastructure.adapter.out.invitation

import com.unicorn.server.domain.invitation.port.out.InvitationTokenGenerator
import com.unicorn.server.domain.invitation.vo.InvitationToken
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class RandomInvitationTokenGenerator : InvitationTokenGenerator {
	override fun generate(): InvitationToken = InvitationToken(buildString(InvitationToken.LENGTH) {
		repeat(InvitationToken.LENGTH) {
			append(ALPHABET[random.nextInt(ALPHABET.length)])
		}
	})

	companion object {
		private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_-"
		private val random = SecureRandom()
	}
}
