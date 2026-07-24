alter table notification_template
    drop constraint if exists notification_template_event_type_check;

alter table notification_template
    add constraint notification_template_event_type_check check (
        event_type in (
            'CIRCLE_JOIN_COMPLETED',
            'SCHEDULE_CREATED',
            'SCHEDULE_DELETED',
            'SCHEDULE_REMINDER_D7',
            'SCHEDULE_REMINDER_D1',
            'SCHEDULE_REMINDER_DDAY_ALL_DAY',
            'SCHEDULE_REMINDER_DDAY_TIMED',
            'SCHEDULE_CONFIRMED_BY_FAMILY',
            'SCHEDULE_CONFIRMATION_REQUESTED',
            'SCHEDULE_FAMILY_NOTIFICATION_REQUESTED'
        )
    );

alter table notification
    drop constraint if exists notification_event_type_check;

alter table notification
    add constraint notification_event_type_check check (
        event_type in (
            'CIRCLE_JOIN_COMPLETED',
            'SCHEDULE_CREATED',
            'SCHEDULE_DELETED',
            'SCHEDULE_REMINDER_D7',
            'SCHEDULE_REMINDER_D1',
            'SCHEDULE_REMINDER_DDAY_ALL_DAY',
            'SCHEDULE_REMINDER_DDAY_TIMED',
            'SCHEDULE_CONFIRMED_BY_FAMILY',
            'SCHEDULE_CONFIRMATION_REQUESTED',
            'SCHEDULE_FAMILY_NOTIFICATION_REQUESTED'
        )
    );

alter table notification_inbox_item
    drop constraint if exists notification_inbox_item_notification_type_check;

alter table notification_inbox_item
    add constraint notification_inbox_item_notification_type_check check (
        notification_type in (
            'CIRCLE_JOIN_COMPLETED',
            'SCHEDULE_CREATED',
            'SCHEDULE_DELETED',
            'SCHEDULE_REMINDER_D7',
            'SCHEDULE_REMINDER_D1',
            'SCHEDULE_REMINDER_DDAY_ALL_DAY',
            'SCHEDULE_REMINDER_DDAY_TIMED',
            'SCHEDULE_CONFIRMED_BY_FAMILY',
            'SCHEDULE_CONFIRMATION_REQUESTED',
            'SCHEDULE_FAMILY_NOTIFICATION_REQUESTED'
        )
    );

insert into notification_template (event_type, title_template, body_template, active, created_at)
values (
    'SCHEDULE_FAMILY_NOTIFICATION_REQUESTED',
    '{sender_name}님이 알림을 보냈어요',
    '{schedule_title} · {d_day}, 확인하고 같이 챙겨봐요!',
    true,
    current_timestamp
);
