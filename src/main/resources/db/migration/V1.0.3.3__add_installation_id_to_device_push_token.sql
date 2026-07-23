alter table device_push_token
    add column installation_id varchar(255);

update device_push_token
set installation_id = 'legacy-' || cast(id as varchar)
where installation_id is null;

alter table device_push_token
    alter column installation_id set not null;

create unique index uk_device_push_token_installation_id
    on device_push_token (installation_id);

alter table device_push_token
    drop column os_notification_permission_granted;

alter table device_push_token
    drop column app_version;
