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
    '11111111-1111-1111-1111-111111111111',
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
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    'KAKAO',
    'kakao-local-member-001',
    'local.member@unicorn.test',
    current_timestamp,
    current_timestamp,
    'flyway-local-seed',
    'flyway-local-seed'
);
