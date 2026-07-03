ALTER TABLE member ALTER COLUMN id TYPE varchar(21);
ALTER TABLE social_account ALTER COLUMN id TYPE varchar(21);
ALTER TABLE social_account ALTER COLUMN member_id TYPE varchar(21);
ALTER TABLE member_agreement ALTER COLUMN member_id TYPE varchar(21);
