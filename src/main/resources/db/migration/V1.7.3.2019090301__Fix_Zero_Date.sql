update tahun_akademik
set tanggal_selesai_nilai = null where tanggal_selesai_nilai < '1000-01-01';

update tahun_akademik_prodi
set selesai_nilai = null where selesai_nilai < '1000-01-01';

update karyawan set tanggal_lahir = null where tanggal_lahir < '1000-01-01';