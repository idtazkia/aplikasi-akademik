CREATE TABLE soal (
  id varchar(36) NOT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  file_upload varchar(255) DEFAULT NULL,
  id_dosen varchar(36) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(36) DEFAULT NULL,
  tanggal_upload date DEFAULT NULL,
  file_approve varchar(255) DEFAULT NULL,
  keterangan_approve varchar(255) DEFAULT NULL,
  status_approve varchar(36) DEFAULT NULL,
  status_soal varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE history_soal (
  id varchar(36) NOT NULL,
  id_soal varchar(36) DEFAULT NULL,
  created_by varchar(255),
  last_modified_by varchar(255),
  created_time DATETIME,
  last_modified_time DATETIME,
  PRIMARY KEY (id)
);

alter table jadwal
add column status_uas varchar(36),
add column status_uts varchar(36);
