package com.unicorn.server.domain.invitation.exception

import com.unicorn.server.common.exception.BusinessException

class InvitationSelfApprovalForbiddenException(invitationId: String) :
	BusinessException(InvitationErrorCode.INVITATION_SELF_APPROVAL_FORBIDDEN, "invitationId=$invitationId")
