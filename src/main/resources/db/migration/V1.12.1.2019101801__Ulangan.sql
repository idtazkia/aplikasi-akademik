CREATE TABLE enable_fiture (
  id varchar(36) NOT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  id_tahun_akademik varchar(255) DEFAULT NULL,
  fitur varchar(36) DEFAULT NULL,
  enable varchar(36) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

alter table krs_detail
add column kode_uts varchar(5),
add column kode_uas varchar(5);
