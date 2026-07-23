create index idx_schedule_confirmation_request
    on schedule (del_yn, need_confirm, created_at);
