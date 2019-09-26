alter table kelas
add column id_kurikulum varchar(36);

alter table matakuliah_kurikulum
add column sds integer,
add column status_skripsi varchar(36);