CREATE TABLE agama (
  id varchar(36) NOT NULL,
  agama varchar(255) DEFAULT NULL,
  status varchar(15) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE lembaga (
  id varchar(36) NOT NULL,
  kode_lembaga varchar(10) DEFAULT NULL,
  nama_lembaga varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE fakultas (
  id varchar(36) NOT NULL,
  id_lembaga varchar(36) DEFAULT NULL,
  kode_fakultas varchar(10) DEFAULT NULL,
  nama_fakultas varchar(255) DEFAULT NULL,
  keterangan varchar(255),
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jurusan (
  id varchar(36) NOT NULL,
  id_fakultas varchar(36) DEFAULT NULL,
  kode_jurusan varchar(10) DEFAULT NULL,
  nama_jurusan varchar(255) DEFAULT NULL,
  keterangan varchar(255),
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jenjang (
  id varchar(36) NOT NULL,
  kode_jenjang varchar(10) DEFAULT NULL,
  nama_jenjang varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE prodi (
  id varchar(36) NOT NULL,
  id_jurusan varchar(36) DEFAULT NULL,
  id_jenjang varchar(36) DEFAULT NULL,
  kode_prodi varchar(10) DEFAULT NULL,
  nama_prodi varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  warna varchar(7) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE program (
  id varchar(36) NOT NULL,
  kode_program varchar(10) DEFAULT NULL,
  nama_program varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE ayah (
  id varchar(36) NOT NULL,
  nama_ayah varchar(255) DEFAULT NULL,
  kebutuhan_khusus varchar(255) DEFAULT NULL,
  tempat_lahir varchar(255) DEFAULT NULL,
  tanggal_lahir date DEFAULT NULL,
  id_jenjang_pendidikan varchar(36) DEFAULT NULL,
  id_pekerjaan varchar(36) DEFAULT NULL,
  penghasilan varchar(255) DEFAULT NULL,
  id_agama varchar(36) DEFAULT NULL,
  status_hidup varchar(2) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE dosen (
  id varchar(36) NOT NULL,
  id_prodi_utama varchar(36) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  id_karyawan varchar(36) DEFAULT NULL,
  absen int(11) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE dosen_prodi (
  id_dosen varchar(36) DEFAULT NULL,
  id_prodi varchar(36) DEFAULT NULL
);

CREATE TABLE gedung (
  id varchar(36) NOT NULL,
  id_kampus varchar(36) DEFAULT NULL,
  kode_gedung varchar(255) DEFAULT NULL,
  nama_gedung varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE grade (
  id varchar(36) NOT NULL,
  nama varchar(2) DEFAULT NULL,
  atas decimal(10,2) DEFAULT NULL,
  bawah decimal(10,2) DEFAULT NULL,
  bobot decimal(10,2) DEFAULT NULL,
  status varchar(255) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE hari (
  id varchar(2) NOT NULL,
  nama_hari varchar(30) DEFAULT NULL,
  nama_hari_eng varchar(30) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE ibu (
  id varchar(36) NOT NULL,
  nama_ibu_kandung varchar(255) DEFAULT NULL,
  kebutuhan_khusus varchar(255) DEFAULT NULL,
  tempat_lahir varchar(255) DEFAULT NULL,
  tanggal_lahir date DEFAULT NULL,
  id_jenjang_pendidikan varchar(36) DEFAULT NULL,
  id_pekerjaan varchar(36) DEFAULT NULL,
  penghasilan varchar(255) DEFAULT NULL,
  id_agama varchar(36) DEFAULT NULL,
  status_hidup varchar(1) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jadwal (
  id varchar(36) NOT NULL,
  id_matakuliah_kurikulum varchar(36) DEFAULT NULL,
  id_hari varchar(36) DEFAULT NULL,
  jam_mulai time DEFAULT NULL,
  jam_selesai time DEFAULT NULL,
  id_tahun_akademik varchar(36) DEFAULT NULL,
  id_tahun_akademik_prodi varchar(36) DEFAULT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  id_dosen_pengampu varchar(36) DEFAULT NULL,
  bobot_uts NUMERIC (3) DEFAULT NULL,
  bobot_uas NUMERIC (3) DEFAULT NULL,
  bobot_tugas NUMERIC (3) DEFAULT NULL,
  bobot_presensi NUMERIC (3    ) DEFAULT NULL,
  id_ruangan varchar(36) DEFAULT NULL,
  id_kelas varchar(36) DEFAULT NULL,
  jumlah_sesi int(11) DEFAULT NULL,
  final_status varchar(1) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jadwal_bobot_tugas (
  id varchar (36) NOT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  nama_tugas varchar(255) DEFAULT NULL,
  bobot float DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jadwal_dosen (
  id varchar(36) NOT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  id_dosen varchar(36) DEFAULT NULL,
  sebagai varchar(1) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE jenis_tinggal (
  id varchar(36) NOT NULL,
  jenis_tinggal varchar(255) DEFAULT NULL,
  keterangan varchar(255),
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE kampus (
  id varchar(36) NOT NULL,
  kode_kampus varchar(255) DEFAULT NULL,
  nama_kampus varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE karyawan (
  id varchar(36) NOT NULL,
  nik varchar(255) DEFAULT NULL,
  nama_karyawan varchar(255) DEFAULT NULL,
  gelar varchar(255) DEFAULT NULL,
  jenis_kelamin varchar(10) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  id_user varchar(36) DEFAULT NULL,
  nidn varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  tanggal_lahir date DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE kelas (
  id varchar(36) NOT NULL,
  kode_kelas varchar(255) DEFAULT NULL,
  nama_kelas varchar(255) DEFAULT NULL,
  keterangan varchar(255),
  id_prodi varchar(36) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE kodepos (
  kodepos varchar(255) DEFAULT NULL,
  kelurahan varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  kecamatan varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  jenis varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  kabupaten varchar(50) CHARACTER SET latin1 DEFAULT NULL,
  propinsi varchar(50) CHARACTER SET latin1 DEFAULT NULL
);

CREATE TABLE konsentrasi (
  id varchar(36) NOT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  kode_konsentrasi varchar(10) DEFAULT NULL,
  nama_konsentrasi varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE krs (
  id varchar(36) NOT NULL,
  id_tahun_akademik varchar(36) DEFAULT NULL,
  id_tahun_akademik_prodi varchar(36) DEFAULT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  nim varchar(255) DEFAULT NULL,
  tanggal_transaksi datetime DEFAULT NULL,
  ip varchar(10) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE krs_detail (
  id varchar(36) NOT NULL,
  id_krs varchar(36) DEFAULT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  id_matakuliah_kurikulum varchar(36) DEFAULT NULL,
  nilai_presensi decimal(10,0) DEFAULT NULL,
  nilai_tugas decimal(10,0) DEFAULT NULL,
  nilai_uts decimal(10,0) DEFAULT NULL,
  nilai_uas decimal(10,0) DEFAULT NULL,
  finalisasi varchar(10) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE krs_nilai_tugas (
  id varchar(36) NOT NULL,
  id_krs_detail varchar(36) DEFAULT NULL,
  id_bobot_tugas varchar(36) DEFAULT NULL,
  nilai float DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  nilai_akhir decimal(10,0) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE kurikulum (
  id varchar(36) NOT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  tahun_kurikulum varchar(255) DEFAULT NULL,
  nama_kurikulum varchar(255) DEFAULT NULL,
  sesi varchar(255) DEFAULT NULL,
  jumlah_sesi int(11) DEFAULT NULL,
  nomor_sk_rektor varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);


CREATE TABLE mahasiswa (
  id varchar(36) NOT NULL,
  angkatan varchar(255) DEFAULT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  id_konsentrasi varchar(36) DEFAULT NULL,
  nim varchar(12) DEFAULT NULL,
  nama varchar(255) DEFAULT NULL,
  status_matrikulasi varchar(1) DEFAULT NULL,
  id_program varchar(36) DEFAULT NULL,
  jenis_kelamin varchar(10) DEFAULT NULL,
  id_agama varchar(36) DEFAULT NULL,
  tempat_lahir varchar(255) DEFAULT NULL,
  tanggal_lahir date DEFAULT NULL,
  id_kelurahan varchar(36) DEFAULT NULL,
  id_kecamatan varchar(36) DEFAULT NULL,
  id_kota_kabupaten varchar(36) DEFAULT NULL,
  id_provinsi varchar(36) DEFAULT NULL,
  id_negara varchar(36) DEFAULT NULL,
  kewarganegaraan varchar(36) DEFAULT NULL,
  nik varchar(255) DEFAULT NULL,
  nisn varchar(255) DEFAULT NULL,
  nama_jalan varchar(255),
  rt varchar(10) DEFAULT NULL,
  rw varchar(10) DEFAULT NULL,
  nama_dusun varchar(255) DEFAULT NULL,
  kodepos varchar(30) DEFAULT NULL,
  jenis_tinggal varchar(255) DEFAULT NULL,
  alat_transportasi varchar(255) DEFAULT NULL,
  telepon_rumah varchar(255) DEFAULT NULL,
  telepon_seluler varchar(255) DEFAULT NULL,
  email_pribadi varchar(255) DEFAULT NULL,
  email_tazkia varchar(255) DEFAULT NULL,
  status_aktif varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  id_user varchar(36) DEFAULT NULL,
  id_ayah varchar(36) DEFAULT NULL,
  id_ibu varchar(36) DEFAULT NULL,
  id_dosen_wali varchar(36) DEFAULT NULL,
  id_wali varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mahasiswa_detail_keluarga (
  id varchar(36) NOT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  id_ayah varchar(36) DEFAULT NULL,
  id_ibu varchar(36) DEFAULT NULL,
  id_wali varchar(36) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mahasiswa_dosen_wali (
  id_mahasiswa varchar(36) NOT NULL,
  id_dosen varchar(36) DEFAULT NULL,
  PRIMARY KEY (id_mahasiswa)
);

CREATE TABLE matakuliah (
  id varchar(36) NOT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  kode_matakuliah varchar(255) DEFAULT NULL,
  nama_matakuliah varchar(255) DEFAULT NULL,
  nama_matakuliah_english varchar(255) DEFAULT NULL,
  singkatan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE matakuliah_kurikulum (
  id varchar(36) NOT NULL,
  id_kurikulum varchar(36) DEFAULT NULL,
  id_matakuliah varchar(36) DEFAULT NULL,
  nomor_urut int(11) DEFAULT NULL,
  wajib varchar(1) DEFAULT NULL,
  responsi varchar(1) DEFAULT NULL,
  semester int(11) DEFAULT NULL,
  matakuliah_kurikulum_semester varchar(255) DEFAULT NULL,
  jumlah_sks int(11) DEFAULT NULL,
  syarat_tugas_akhir varchar(1) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE presensi_dosen (
  id varchar(36) NOT NULL,
  id_tahun_akademik varchar(36) DEFAULT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  waktu_masuk datetime DEFAULT NULL,
  waktu_selesai datetime DEFAULT NULL,
  status_presensi varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  id_dosen varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE presensi_mahasiswa (
  id varchar(36) NOT NULL,
  id_krs_detail varchar(36) DEFAULT NULL,
  id_sesi_kuliah varchar(36) DEFAULT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  waktu_masuk datetime DEFAULT NULL,
  waktu_keluar datetime DEFAULT NULL,
  status_presensi varchar(50) DEFAULT NULL,
  catatan varchar(255),
  rating int(11) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE rekap_kehadiran_mahasiswa (
  id varchar(36) NOT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  hadir int(11) DEFAULT NULL,
  mangkir int(11) DEFAULT NULL,
  sakit int(11) DEFAULT NULL,
  izin int(11) DEFAULT NULL,
  terlambat int(11) DEFAULT NULL,
  lain_lain int(11) DEFAULT NULL,
  terakhir_update datetime DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE rekening_bank (
  id varchar(36) NOT NULL,
  nomor_rekening varchar(255) DEFAULT NULL,
  nama_bank varchar(255) DEFAULT NULL,
  keterangan varchar(255),
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE ruangan (
  id varchar(36) NOT NULL,
  id_gedung varchar(36) DEFAULT NULL,
  kode_ruangan varchar(255) DEFAULT NULL,
  id_jenis_ruangan varchar(36) DEFAULT NULL,
  nama_ruangan varchar(255) DEFAULT NULL,
  lantai varchar(50) DEFAULT NULL,
  kapasitas numeric(4) DEFAULT NULL,
  keterangan varchar(255),
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE ruangan_jenis (
  id varchar(36) NOT NULL,
  jenis_ruangan varchar(255) DEFAULT NULL,
  keterangan varchar(255) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE s_permission (
  id varchar(36) NOT NULL,
  permission_label varchar(255) NOT NULL,
  permission_value varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY permission_value (permission_value)
);

CREATE TABLE s_role (
  id varchar(36) NOT NULL,
  description varchar(36) DEFAULT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY name (name)
);

CREATE TABLE s_role_permission (
  id_role varchar(36) NOT NULL,
  id_permission varchar(36) NOT NULL,
  PRIMARY KEY (id_role,id_permission),
  KEY id_permission (id_permission),
  FOREIGN KEY (id_permission) REFERENCES s_permission (id),
  FOREIGN KEY (id_role) REFERENCES s_role (id)
);

CREATE TABLE s_user (
  id varchar(36) NOT NULL,
  username varchar(255) NOT NULL,
  active tinyint(1) NOT NULL,
  id_role varchar(36) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username),
  KEY id_role (id_role),
  FOREIGN KEY (id_role) REFERENCES s_role (id)
);

CREATE TABLE s_user_password (
  id varchar(36) NOT NULL,
  id_user varchar(36) NOT NULL,
  password varchar(255) NOT NULL,
  PRIMARY KEY (id),
  KEY id_user (id_user),
  FOREIGN KEY (id_user) REFERENCES s_user (id)
);

CREATE TABLE sesi_kuliah (
  id varchar(36 NOT NULL,
  id_jadwal varchar(36) DEFAULT NULL,
  berita_acara varchar(255),
  waktu_mulai datetime DEFAULT NULL,
  waktu_selesai datetime DEFAULT NULL,
  id_presensi_dosen varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tahun_akademik (
  id varchar(36) NOT NULL,
  kode_tahun_akademik varchar(10) DEFAULT NULL,
  nama_tahun_akademik varchar(255) DEFAULT NULL,
  tanggal_mulai date DEFAULT NULL,
  tanggal_selesai date DEFAULT NULL,
  tanggal_mulai_krs date DEFAULT NULL,
  tanggal_selesai_krs date DEFAULT NULL,
  tanggal_mulai_kuliah date DEFAULT NULL,
  tanggal_selesai_kuliah date DEFAULT NULL,
  tanggal_mulai_uts date DEFAULT NULL,
  tanggal_selesai_uts date DEFAULT NULL,
  tanggal_mulai_uas date DEFAULT NULL,
  tanggal_selesai_uas date DEFAULT NULL,
  tanggal_mulai_nilai date DEFAULT NULL,
  tanggal_selesai_nilai date DEFAULT NULL,
  tahun varchar(4) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE tahun_akademik_prodi (
  id varchar(36) NOT NULL,
  id_tahun_akademik varchar(36) DEFAULT NULL,
  mulai_krs date DEFAULT NULL,
  selesai_krs date DEFAULT NULL,
  mulai_kuliah date DEFAULT NULL,
  selesai_kuliah date DEFAULT NULL,
  mulai_uts date DEFAULT NULL,
  selesai_uts date DEFAULT NULL,
  mulai_uas date DEFAULT NULL,
  selesai_uas date DEFAULT NULL,
  mulai_nilai date DEFAULT NULL,
  selesai_nilai date DEFAULT NULL,
  id_prodi varchar(36) DEFAULT NULL,
  id_kurikulum varchar(36) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE wali (
  id varchar(36) NOT NULL,
  nama_wali varchar(255) DEFAULT NULL,
  kebutuhan_khusus varchar(255) DEFAULT NULL,
  tempat_lahir varchar(255) DEFAULT NULL,
  tanggal_lahir date DEFAULT NULL,
  id_jenjang_pendidikan varchar(36) DEFAULT NULL,
  id_pekerjaan varchar(36) DEFAULT NULL,
  id_penghasilan varchar(36) DEFAULT NULL,
  id_agama varchar(36) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  id_mahasiswa varchar(36) DEFAULT NULL,
  PRIMARY KEY (id)
);
