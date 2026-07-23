create index idx_schedule_reminder_daily
    on schedule (del_yn, start_date, created_at);

create index idx_schedule_reminder_timed
    on schedule (del_yn, start_date, start_time, created_at);
