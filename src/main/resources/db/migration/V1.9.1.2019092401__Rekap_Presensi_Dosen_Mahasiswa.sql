alter table jadwal_dosen
add column jumlah_kehadiran integer not null default 0,
add column jumlah_terlambat integer not null default 0,
add column jumlah_mangkir integer not null default 0,
add column jumlah_izin integer not null default 0,
add column jumlah_sakit integer not null default 0;

alter table krs_detail
add column jumlah_kehadiran integer not null default 0,
add column jumlah_terlambat integer not null default 0,
add column jumlah_mangkir integer not null default 0,
add column jumlah_izin integer not null default 0,
add column jumlah_sakit integer not null default 0;

-- select concat(id_dosen, ' - ', id_jadwal) as x, count(status_jadwal_dosen) as y from jadwal_dosen group by x having y > 1;
alter table jadwal_dosen add unique `jadwal_dosen_unique` (id_jadwal, id_dosen);