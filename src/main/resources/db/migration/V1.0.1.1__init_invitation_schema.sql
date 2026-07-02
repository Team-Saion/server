create table invitation
(
    id             varchar(36) not null,
    type           varchar(20) not null,
    target_id      varchar(36) not null,
    token          varchar(64) not null,
    inviter_id     varchar(36) not null,
    invite_to_name varchar(10),
    message        varchar(50),
    status         varchar(20) not null,
    expires_at     timestamp   not null,
    del_yn         varchar(1)  not null default 'N',
    created_at     timestamp   not null,
    updated_at     timestamp,
    created_by     varchar(100),
    updated_by     varchar(100),
    constraint pk_invitation primary key (id),
    constraint uq_invitation_token unique (token)
);

create index idx_invitation_type_target_id on invitation (type, target_id);
create index idx_invitation_inviter_id on invitation (inviter_id);
create index idx_invitation_status_expires_at on invitation (status, expires_at);

create table invitation_dispatch_log
(
    id            varchar(36) not null,
    invitation_id varchar(36) not null,
    channel       varchar(20) not null,
    dispatched_at timestamp   not null,
    created_at    timestamp   not null,
    updated_at    timestamp,
    created_by    varchar(100),
    updated_by    varchar(100),
    constraint pk_invitation_dispatch_log primary key (id)
);

create index idx_invitation_dispatch_log_invitation_id on invitation_dispatch_log (invitation_id);
create index idx_invitation_dispatch_log_channel on invitation_dispatch_log (channel);

create table invitation_click_log
(
    id            varchar(36) not null,
    invitation_id varchar(36) not null,
    clicked_at    timestamp   not null,
    created_at    timestamp   not null,
    updated_at    timestamp,
    created_by    varchar(100),
    updated_by    varchar(100),
    constraint pk_invitation_click_log primary key (id)
);

create index idx_invitation_click_log_invitation_id on invitation_click_log (invitation_id);

create table invitation_redemption_log
(
    id                 varchar(36) not null,
    invitation_id      varchar(36) not null,
    redeemer_member_id varchar(36) not null,
    redeemed_at        timestamp   not null,
    created_at         timestamp   not null,
    updated_at         timestamp,
    created_by         varchar(100),
    updated_by         varchar(100),
    constraint pk_invitation_redemption_log primary key (id),
    constraint uq_invitation_redemption_invitation_redeemer unique (invitation_id, redeemer_member_id)
);

create index idx_invitation_redemption_log_invitation_id on invitation_redemption_log (invitation_id);
create index idx_invitation_redemption_log_redeemer on invitation_redemption_log (redeemer_member_id);
