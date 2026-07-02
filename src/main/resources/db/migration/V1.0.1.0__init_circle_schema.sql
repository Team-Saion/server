create table circle (
    id varchar(36) not null,
    name varchar(20) not null,
    owner_id varchar(36) not null,
    del_yn varchar(1) not null default 'N',
    created_at timestamp not null,
    updated_at timestamp,
    created_by varchar(100),
    updated_by varchar(100),
    constraint pk_circle primary key (id)
);

create index idx_circle_owner_id on circle (owner_id);
create index idx_circle_del_yn on circle (del_yn);

create table circle_member (
    id varchar(36) not null,
    circle_id varchar(36) not null,
    member_id varchar(36) not null,
    nickname varchar(30) not null,
    role varchar(20) not null,
    status varchar(20) not null,
    joined_at timestamp not null,
    left_at timestamp,
    del_yn varchar(1) not null default 'N',
    created_at timestamp not null,
    updated_at timestamp,
    created_by varchar(100),
    updated_by varchar(100),
    constraint pk_circle_member primary key (id),
    constraint uq_circle_member_circle_member unique (circle_id, member_id),
    constraint fk_circle_member_circle foreign key (circle_id) references circle (id)
);

create index idx_circle_member_member_id on circle_member (member_id);
create index idx_circle_member_circle_id on circle_member (circle_id);
