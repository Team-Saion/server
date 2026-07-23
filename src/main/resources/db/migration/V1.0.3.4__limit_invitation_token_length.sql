delete from invitation
where char_length(token) <> 6;

alter table invitation
    alter column token type varchar(6);

alter table invitation
    add constraint ck_invitation_token_length
        check (char_length(token) = 6);
