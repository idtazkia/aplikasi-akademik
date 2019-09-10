insert into jadwal_dosen (id, status_jadwal_dosen, id_dosen, id_jadwal)
(select uuid() as id, 'PENGAMPU' as status_jadwal_dosen, id_dosen_pengampu, id from jadwal where id_dosen_pengampu is not null)