alter table jadwal
    add column created_by varchar(255),
    add column last_modified_by varchar(255),
    add column created_time DATETIME,
    add column last_modified_time DATETIME;

alter table krs_detail
    add column created_by varchar(255),
    add column last_modified_by varchar(255),
    add column created_time DATETIME,
    add column last_modified_time DATETIME;

