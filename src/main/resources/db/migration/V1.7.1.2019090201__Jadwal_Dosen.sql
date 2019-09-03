alter table jadwal_dosen
add column id varchar(36),
add column status_jadwal_dosen varchar(100) not null;

update jadwal_dosen
set status_jadwal_dosen = 'TEAM';

update jadwal_dosen
set id = uuid();

alter table jadwal_dosen
add primary key (id);

alter table smile.jadwal_dosen
modify id_dosen varchar(36) not null;

alter table smile.jadwal_dosen
modify id_jadwal varchar(36) not null;