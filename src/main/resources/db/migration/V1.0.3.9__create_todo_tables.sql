create sequence if not exists todo_seq start with 1 increment by 1;

create table todo (
    todo_id varchar(19) not null,
    schedule_id varchar(19) not null,
    circle_id varchar(21) not null,
    title varchar(30) not null,
    created_by varchar(100) not null,
    del_yn varchar(1) not null default 'N',
    constraint pk_todo primary key (todo_id),
    constraint fk_todo_schedule foreign key (schedule_id) references schedule (schedule_id),
    constraint fk_todo_circle foreign key (circle_id) references circle (id)
);

create index idx_todo_schedule on todo (schedule_id, del_yn);

create table todo_member (
    todo_id varchar(19) not null,
    member_id varchar(100) not null,
    checked boolean not null default false,
    constraint pk_todo_member primary key (todo_id, member_id),
    constraint fk_todo_member_todo foreign key (todo_id) references todo (todo_id)
);
