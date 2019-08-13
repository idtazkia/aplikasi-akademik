create table komponen_biaya (
id varchar(36),
nama varchar(255),
keterangan varchar(255),
status varchar(255),
satuan varchar(255),
primary key(id)
);

create table nilai_komponen_biaya (
id varchar(36),
id_komponen_biaya varchar(36),
id_tahun_akademik varchar(36),
id_angkatan_mahasiswa varchar(36),
id_prodi varchar(36),
id_program varchar(36),
status varchar(255),
amount NUMERIC (19,2),
dikali_sks varchar(255),
primary key(id)
);

create table tagihan_mahasiswa (
id varchar(36),
tanggal_tagih DATE,
batas_waktu DATE,
id_komponen_biaya varchar(36),
id_mahasiswa varchar(36),
id_tahun_akademik varchar(36),
lunas varchar(45),
qty int(11),
amount NUMERIC (19,2),
primary key(id)
);


create table jenis_diskon (
id varchar(36),
nama varchar(255),
keterangan varchar(255),
primary key(id)
);

create table diskon (
id varchar(36),
id_tagihan varchar(36),
id_jenis_diskon varchar(36),
amount NUMERIC (19,2),
primary key(id)
);

create table rencana_pembayaran (
id varchar(36),
id_tagihan varchar (36),
tanggal_jatuh_tempo DATE,
amount NUMERIC (19,2),
lunas varchar (50),
primary key(id)
);

create table rencana_pembayaran_detail (
id varchar(36),
id_rencana_pembayaran varchar (36),
pembayaran_ke varchar (25),
id_tagihan varchar(36),
amount NUMERIC (19,2),
status_bayar varchar (25),
lunas varchar (50),
keterangan varchar(50),
primary key(id)
);

create table pembayaran_mahasiswa (
id varchar(36),
id_tagihan varchar(36),
waktu_bayar DATE,
jenis_bayar varchar(255),
nomor_rekening varchar(255),
amount NUMERIC (19,2),
referensi varchar(255),
id_rencana_pembayaran varchar (36),
primary key(id)
);

create table histori_tagihan (
id varchar(36),
id_tagihan varchar(36),
id_user varchar(36),
tindakan varchar(255),
nilai_lama NUMERIC (19,2),
nilai_baru NUMERIC (19,2),
waktu_bayar DATE,
primary key(id)
);

create table histori_rencana_detail (
id varchar(36),
id_rencana_detail varchar(36),
id_user varchar(36),
amount NUMERIC (19,2),
waktu_diubah DATE,
primary key(id)
);

create table kelas_mahasiswa (
id varchar (36),
id_mahasiswa varchar (36),
id_kelas varchar(36),
status varchar (25),
primary key(id)
);

create table sesi (
id varchar (36),
sesi varchar (255),
nama_sesi varchar (255),
id_jenjang varchar(255),
sks int(3),
jam_mulai time,
jam_selesai time,
primary key(id)
);

insert into sesi values('01','1', 'Sesi 1','01','3','07:00:00','09:30:00');
insert into sesi values('02','1', 'Sesi 1 (2 SKS Alternative 1)','01','2','07:50:00','09:30:00');
insert into sesi values('03','1', 'Sesi 1 (2 SKS Alternative 2)','01','3','07:00:00','08:40:00');
insert into sesi values('04','2', 'Sesi 2','01','3','09:31:00','12:00:00');
insert into sesi values('05','2', 'Sesi 2 (2 SKS Alternative 1)','01','2','10:30:00','12:00:00');
insert into sesi values('06','2', 'Sesi 2 (2 SKS Alternative 2)','01','2','09:31:00','11:10:00');
insert into sesi values('07','3', 'Sesi 3','01','3','13:00:00','15:30:00');
insert into sesi values('08','3', 'Sesi 3 (2 SKS Alternative 1)','01','2','13:50:00','15:30:00');
insert into sesi values('09','3', 'Sesi 3 (2 SKS Alternative 2)','01','2','13:00:00','14:40:00');
insert into sesi values('10','4', 'Sesi 4','01','3','15:31:00','18:00:00');
insert into sesi values('11','4', 'Sesi 4 (2 SKS Alternative 1)','01','2','16:30:00','18:00:00');
insert into sesi values('12','4', 'Sesi 4 (2 SKS Alternative 2)','01','2','15:31:00','17:10:00');

alter table mahasiswa
add column id_kurikulum varchar(36);
alter table mahasiswa
add column terakhir_update DATE;
alter table jadwal
add column sesi varchar(255);


create table prasyarat (
id varchar (36),
id_matakuliah_kurikulum varchar (36),
id_matakuliah varchar (36),
id_matakuliah_kurikulum_pras varchar(36),
id_matakuliah_pras varchar(36),
nilai decimal(10,2) DEFAULT NULL,
status varchar(255),
primary key(id)
);

alter table prodi
add column kode_matakuliah varchar(255);
alter table prodi
add column id_program varchar(36);
alter table fakultas
add column kode_matakuliah varchar(255);

alter table matakuliah_kurikulum
add column id_konsentrasi varchar(36);
alter table matakuliah_kurikulum
add column sks_minimal int(3);
alter table matakuliah_kurikulum
add column ipk_minimal decimal(10,2) DEFAULT NULL;
alter table matakuliah_kurikulum
add column silabus varchar(255);
alter table tahun_akademik
add column jenis varchar(255);

create table matakuliah_kurikulum_program (
  id_matakuliah_kurikulum VARCHAR (36)NOT NULL ,
  id_program VARCHAR (36)NOT NULL ,
  PRIMARY KEY (id_matakuliah_kurikulum, id_program)
);

create table prodi_program (
  id_prodi VARCHAR (36)NOT NULL ,
  id_program VARCHAR (36)NOT NULL ,
  PRIMARY KEY (id_prodi, id_program),
  FOREIGN KEY (id_prodi) REFERENCES prodi(id),
  FOREIGN KEY (id_program) REFERENCES program(id)
);

alter table jadwal
add column akses varchar(255);
alter table matakuliah_kurikulum
add column akses varchar(255);