create table transportasi (
id varchar(255),
nama varchar(255),
primary key(id)
);

insert into transportasi values('01','Jalan Kaki');
insert into transportasi values('02','Angkutan Umum/Bus/Pete-pete');
insert into transportasi values('03','Mobil/Bus Antar jemput');
insert into transportasi values('04','Kereta Api');
insert into transportasi values('05','Ojek');
insert into transportasi values('06','Andong/BendiSado/Dokar/Delman/Becak');
insert into transportasi values('07','Perahu Penyebrangan/rakit/getek');
insert into transportasi values('08','Kuda');
insert into transportasi values('09','Sepeda');
insert into transportasi values('10','Sepeda Motor');
insert into transportasi values('11','mobil Pribadi');
insert into transportasi values('12','Lainnya');

create table kebutuhan_khusus (
id varchar(255),
kode varchar(5),
nama varchar(36),
PRIMARY KEY (id)
);


insert into kebutuhan_khusus values ('1','A','Tuna Netra');
insert into kebutuhan_khusus values ('2','B','Tuna Rungu');
insert into kebutuhan_khusus values ('3','C','Tuna Grahita Ringan');
insert into kebutuhan_khusus values ('4','C1','Tuna Grahita Sedang');
insert into kebutuhan_khusus values ('5','D','Tuna Daksa Ringan');
insert into kebutuhan_khusus values ('6','D1','Tuna Daksa Sedang');
insert into kebutuhan_khusus values ('7','E','Tuna Laras');
insert into kebutuhan_khusus values ('8','F','Tuna Wicara');
insert into kebutuhan_khusus values ('9','H','Hiperaktif');
insert into kebutuhan_khusus values ('10','I','Cerdas Istimewa');
insert into kebutuhan_khusus values ('11','J','Bakat Istimewa');
insert into kebutuhan_khusus values ('12','K','Kesulitan Belajar');
insert into kebutuhan_khusus values ('13','N','Narkoba');
insert into kebutuhan_khusus values ('14','O','Indigo');
insert into kebutuhan_khusus values ('15','P','Down Syndrome');
insert into kebutuhan_khusus values ('16','Q','Autis');

create table pendidikan (
  id VARCHAR (36),
  nama VARCHAR(36) NOT NULL,
  PRIMARY KEY (id)
);

insert into pendidikan values ('00','Tidak Sekolah');
insert into pendidikan values ('01','PAUD');
insert into pendidikan values ('02','TK / Sederajat');
insert into pendidikan values ('03','Putus SD');
insert into pendidikan values ('04','SD / Sederajat');
insert into pendidikan values ('05','SMP / Sederajat');
insert into pendidikan values ('06','SMA / Sederajat');
insert into pendidikan values ('07','Paket A');
insert into pendidikan values ('08','Paket B');
insert into pendidikan values ('09','Paket c');
insert into pendidikan values ('10','D1');
insert into pendidikan values ('11','D2');
insert into pendidikan values ('12','D3');
insert into pendidikan values ('13','D4');
insert into pendidikan values ('14','S1');
insert into pendidikan values ('15','profesi');
insert into pendidikan values ('16','Sp - 1');
insert into pendidikan values ('17','S2');
insert into pendidikan values ('18','S2 Terapan');
insert into pendidikan values ('19','Sp-2');
insert into pendidikan values ('20','S3');
insert into pendidikan values ('21','S3 Terapan');
insert into pendidikan values ('22','Non Formal');
insert into pendidikan values ('23','Informal');
insert into pendidikan values ('24','Lainnya');

create table pekerjaan (
  id VARCHAR (36),
  nama VARCHAR(36) NOT NULL,
  PRIMARY KEY (id)
);

insert into pekerjaan VALUES ('01', 'Tidak Bekerja');
insert into pekerjaan VALUES ('02', 'Nelayan');
insert into pekerjaan VALUES ('03', 'Petani');
insert into pekerjaan VALUES ('04', 'Peternak');
insert into pekerjaan VALUES ('05', 'PNS/TNI/Polri');
insert into pekerjaan VALUES ('06', 'Karyawan Swasta');
insert into pekerjaan VALUES ('07', 'Pedagang Kecil');
insert into pekerjaan VALUES ('08', 'Pedagang Besar');
insert into pekerjaan VALUES ('09', 'Wiraswasta');
insert into pekerjaan VALUES ('10', 'Wirausaha');
insert into pekerjaan VALUES ('11', 'Buruh');
insert into pekerjaan VALUES ('12', 'Pensiunan');
insert into pekerjaan VALUES ('13', 'Sudah Meninggal');
insert into pekerjaan VALUES ('14', 'lainnya');

create table penghasilan (
  id VARCHAR (36),
  nama VARCHAR(36) NOT NULL,
  PRIMARY KEY (id)
);

insert into penghasilan values ('1','Kurang dari Rp.500.000');
insert into penghasilan values ('2','Rp.500.000 - Rp.999.999');
insert into penghasilan values ('3','Rp.1.000.000 - Rp.1.999.999');
insert into penghasilan values ('4','Rp.2.000.000 - Rp.4.999.999');
insert into penghasilan values ('5','Rp.5.000.000 - Rp.20.000.000');
insert into penghasilan values ('6','Lebih dari Rp.20.000.000');

create table sesi_jadwal(
id varchar(255),
nama varchar(255),
jam_masuk time,
jam_keluar time,
primary key(id)
);