alter table matakuliah_kurikulum
add column konsep_note varchar(36),
add column sempro varchar(36);

CREATE TABLE note (
  id varchar(36) NOT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  judul varchar(255) DEFAULT NULL,
  judul_inggris varchar(255) DEFAULT NULL,
  id_dosen1 varchar(36) DEFAULT NULL,
  id_dosen2 varchar(36) DEFAULT NULL,
  tanggal_input date DEFAULT NULL,
  status varchar(36) DEFAULT NULL,
  id_tahun_akademik varchar(255) DEFAULT NULL,
  file_upload varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  tanggal_approve date DEFAULT NULL,
  user_approve varchar(36) DEFAULT NULL,
  tanggal_reject date DEFAULT NULL,
  user_reject varchar(36) DEFAULT NULL,
  jenis varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);
