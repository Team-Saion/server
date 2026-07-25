alter table withdrawal_log add column social_provider varchar(20);

update withdrawal_log
set social_provider = (
    select social_account.provider
    from social_account
    where social_account.member_id = withdrawal_log.member_id
)
where exists (
    select 1
    from social_account
    where social_account.member_id = withdrawal_log.member_id
);
