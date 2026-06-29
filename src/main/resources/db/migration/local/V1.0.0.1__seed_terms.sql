insert into term (
    term_code,
    title,
    content_url,
    version,
    required_yn,
    effective_at,
    created_at,
    updated_at
) values
    ('AGE_OVER_14', '만 14세 이상 확인', 'https://local.unicorn.test/terms/age-over-14/v1', 1, 'Y', timestamp '2026-01-01 00:00:00', current_timestamp, current_timestamp),
    ('SERVICE_USE', '서비스 이용약관', 'https://local.unicorn.test/terms/service-use/v1', 1, 'Y', timestamp '2026-01-01 00:00:00', current_timestamp, current_timestamp),
    ('PRIVACY_COLLECTION', '개인정보 수집 및 이용 동의', 'https://local.unicorn.test/terms/privacy-collection/v1', 1, 'Y', timestamp '2026-01-01 00:00:00', current_timestamp, current_timestamp),
    ('MARKETING', '마케팅 정보 수신 동의', 'https://local.unicorn.test/terms/marketing/v1', 1, 'N', timestamp '2026-01-01 00:00:00', current_timestamp, current_timestamp);
