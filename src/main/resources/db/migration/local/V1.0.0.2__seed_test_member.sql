insert into member (
    id,
    email,
    name,
    nickname,
    role,
    profile_image_key,
    status,
    deleted_at,
    created_at,
    updated_at,
    created_by,
    updated_by
) values (
    'MB20260101000000001',
    'local.member@unicorn.test',
    'Local Member',
    'local_member',
    'MEMBER',
    null,
    'ACTIVE',
    null,
    current_timestamp,
    current_timestamp,
    'flyway-local-seed',
    'flyway-local-seed'
);

insert into social_account (
    id,
    member_id,
    provider,
    provider_id,
    email,
    created_at,
    updated_at,
    created_by,
    updated_by
) values (
    'SA20260101000000001',
    'MB20260101000000001',
    'KAKAO',
    'kakao-local-member-001',
    'local.member@unicorn.test',
    current_timestamp,
    current_timestamp,
    'flyway-local-seed',
    'flyway-local-seed'
);
