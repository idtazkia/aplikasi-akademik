package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.EdomDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.StudentDto;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail, String> {
    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.hari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.hari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query(value = "select aaaa.id,bbbb.nama_hari,cccc.nama_kelas,aaaa.jam_mulai,aaaa.jam_selesai,aaaa.sks,eeee.nama_karyawan as dosen,aaaa.nama_matakuliah,aaaa.kapasitas,'0' as mhsw,cmkk,bmkk as bmk,prass,matkul_prass as bmkk,nama_prass from (select aaa.*,bbb.id_jadwal as cmkk,ccc.kode_matakuliah bmkk,ddd.kode_matakuliah as prass,eee.kode_matakuliah as matkul_prass,eee.nama_matakuliah as nama_prass from(select * from(select a.*,b.id_matakuliah,c.kode_matakuliah,b.jumlah_sks as sks,c.nama_matakuliah from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='TERTUTUP' and a.id_kelas=?2 and a.id_hari is not null union select a.*,b.id_matakuliah,c.kode_matakuliah,b.jumlah_sks as sks,c.nama_matakuliah from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='PRODI' and a.id_prodi=?3 and a.id_hari is not null union select a.*,b.id_matakuliah,c.kode_matakuliah,b.jumlah_sks as sks,c.nama_matakuliah from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='UMUM' and a.id_hari is not null group by a.id)aa)as aaa left join(select a.id_jadwal,a.id_matakuliah_kurikulum,b.id_matakuliah,c.kode_matakuliah,c.nama_matakuliah from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.id_mahasiswa=?4 group by a.id_jadwal)bbb on aaa.id=bbb.id_jadwal or aaa.id_matakuliah_kurikulum=bbb.id_matakuliah_kurikulum or aaa.id_matakuliah=bbb.id_matakuliah or aaa.kode_matakuliah=bbb.kode_matakuliah or aaa.nama_matakuliah=bbb.nama_matakuliah left join(select a.id_matakuliah_kurikulum,b.id_matakuliah,c.kode_matakuliah,c.nama_matakuliah from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.status='AKTIF' and a.bobot >= 3.00 and a.id_mahasiswa=?4 group by id_jadwal)ccc on aaa.id_matakuliah_kurikulum=ccc.id_matakuliah_kurikulum or aaa.id_matakuliah=ccc.id_matakuliah or aaa.kode_matakuliah=ccc.kode_matakuliah left join(select a.*,c.kode_matakuliah,c.nama_matakuliah from prasyarat as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum_pras=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.status='AKTIF')eee on aaa.id_matakuliah_kurikulum=eee.id_matakuliah_kurikulum left join(select aa.* from(select a.*,b.id_matakuliah,c.kode_matakuliah,c.nama_matakuliah from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.status='AKTIF' and a.id_mahasiswa=?4)aa inner join(select a.*,c.kode_matakuliah,c.nama_matakuliah from prasyarat as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum_pras=b.id inner join matakuliah as c on a.id_matakuliah_pras=c.id where a.status='AKTIF' group by c.kode_matakuliah)bb on aa.id_matakuliah_kurikulum=bb.id_matakuliah_kurikulum_pras or aa.id_matakuliah=bb.id_matakuliah_pras or aa.kode_matakuliah=bb.kode_matakuliah where aa.bobot >= bb.nilai group by aa.id)ddd on eee.id_matakuliah_kurikulum = ddd.id_matakuliah_kurikulum or eee.id_matakuliah=ddd.id_matakuliah or eee.kode_matakuliah=ddd.kode_matakuliah where bbb.id_jadwal is null and ccc.kode_matakuliah is null)aaaa inner join hari as bbbb on aaaa.id_hari=bbbb.id inner join kelas as cccc on aaaa.id_kelas=cccc.id inner join dosen as dddd on aaaa.id_dosen_pengampu=dddd.id inner join karyawan as eeee on dddd.id_karyawan=eeee.id group by aaaa.id", nativeQuery = true)
    List<Object[]> pilihanKrs(TahunAkademik tahunAkademik, Kelas kelas, Prodi prodi, Mahasiswa mahasiswa);

    @Query("select sum (kd.matakuliahKurikulum.jumlahSks) from KrsDetail kd where kd.status = :status and kd.krs= :krs")
    Long jumlahSks (@Param("status") StatusRecord statusRecord,@Param("krs")Krs krs);

    @Query(value = "select e.nama_matakuliah, count(c.id) as total, round(((count(c.id)*100)/3),2) as persentase  from (select * from krs_detail where status='AKTIF' and id_mahasiswa=?1)a inner join (select * from krs where status='AKTIF' and id_mahasiswa=?1 and id_tahun_akademik=?2)b on a.id_krs = b.id inner join (select aa.* from presensi_mahasiswa as aa inner join sesi_kuliah as bb on aa.id_sesi_kuliah = bb.id inner join presensi_dosen as cc on bb.id_presensi_dosen = cc.id where cc.status='AKTIF' and aa.id_mahasiswa = ?1 and aa.status = 'AKTIF' and aa.status_presensi in ('MANGKIR','TERLAMBAT'))c on a.id = c.id_krs_detail inner join matakuliah_kurikulum as d on a.id_matakuliah_kurikulum=d.id inner join matakuliah as e on d.id_matakuliah=e.id group by id_krs_Detail", nativeQuery = true)
    List<Object[]> persentaseKehadiran(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord,StatusRecord statusEdom);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.report.EdomDto (kd.id,kd.jadwal.dosen.karyawan.namaKaryawan,kd.matakuliahKurikulum.matakuliah.namaMatakuliah) from KrsDetail kd where kd.mahasiswa = :mahasiswa and kd.krs.tahunAkademik = :akademik and kd.status = :status and kd.statusEdom = :edom")
    List<EdomDto>cariEdom(@Param("mahasiswa")Mahasiswa mahasiswa,@Param("akademik")TahunAkademik tahunAkademik,@Param("status")StatusRecord status,@Param("edom") StatusRecord edom);

    @Query(value = "select b.id,mk.kode_matakuliah as kode ,mk.nama_matakuliah_english as matakuliah,b.nilai_presensi as presensi ,b.nilai_tugas as tugas,b.nilai_uts as uts,b.nilai_uas as uas,coalesce (b.nilai_akhir,0) as nilaiAkhir,coalesce(c.bobot,0)  as bobot,coalesce (c.nama,'E') as grade from krs as a inner join krs_detail as b on a.id = b.id_krs left join grade as c on b.nilai_akhir >= c.bawah and b.nilai_akhir <= c.atas inner join matakuliah_kurikulum as m on b.id_matakuliah_kurikulum = m.id inner join matakuliah as mk on m.id_matakuliah = mk.id where a.id_tahun_akademik= ?1 and a.id_mahasiswa= ?2 and a.status='AKTIF' and b.status='aktif'", nativeQuery = true)
    List<DataKhsDto> getKhs(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.namaMatakuliah = :nama and kd.nilaiAkhir >= 55")
    List<BigDecimal> nilaiMagang(@Param("status")StatusRecord statusRecord, @Param("mahasiswa") Mahasiswa mahasiswa, @Param("nama")String nama);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.singkatan = :singkatan and kd.nilaiAkhir >= 55")
    List<BigDecimal> nilaiMetolit(@Param("status")StatusRecord statusRecord,@Param("mahasiswa") Mahasiswa mahasiswa,@Param("singkatan")String singkatan);

    @Query(value = "select aa.status_presensi, coalesce(aa.waktu_masuk,bb.waktu_mulai) as masuk, coalesce(aa.waktu_keluar,bb.waktu_selesai)as keluar, bb.berita_acara from (select a.* from presensi_mahasiswa as a inner join krs_detail as b on a.id_krs_detail=b.id inner join krs as c on b.id_krs=c.id where a.id_mahasiswa=?1 and c.id_tahun_akademik=?2 and a.status='AKTIF' and b.status='AKTIF' and c.status='AKTIF' and b.id_jadwal=?3)aa inner join sesi_kuliah as bb on aa.id_sesi_kuliah=bb.id inner join (select * from presensi_dosen where status='AKTIF' and id_jadwal=?3) cc on bb.id_presensi_dosen=cc.id order by bb.waktu_mulai", nativeQuery = true)
    List<Object[]> detailPresensi(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,Jadwal jadwal);

    @Query("select kd.id from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    String validasiKrs(@Param("jadwal") Jadwal jadwal);

    @Query("select count (kd) from KrsDetail kd where kd.matakuliahKurikulum = :matakuliah and kd.status = 'AKTIF'")
    Long hitungMatakuliahKurikulum(@Param("matakuliah") MatakuliahKurikulum matakuliahKurikulum);

    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal j, StatusRecord aktif, TahunAkademik byStatus);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal j, StatusRecord aktif);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNimAsc(Jadwal j, StatusRecord aktif);

    @Query("select kd from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF' and kd.krs.tahunAkademik = :tahun and kd.mahasiswa = :mahasiswa")
    KrsDetail cariKrs(@Param("jadwal")Jadwal jadwal,@Param("tahun")TahunAkademik tahun,@Param("mahasiswa")Mahasiswa mahasiswa);

    List<KrsDetail> findByJadwalAndStatus(Jadwal jadwal, StatusRecord aktif);

    @Query("select kd.id from KrsDetail kd where kd.mahasiswa.id = :id and kd.status = :status and kd.matakuliahKurikulum.matakuliah.namaMatakuliah like %:nama%")
    String idKrsDetail(@Param("id")String id,@Param("status")StatusRecord statusRecord,@Param("nama")String nama);

    KrsDetail findByMahasiswaAndJadwalAndStatus(Mahasiswa byNim, Jadwal jadwal, StatusRecord aktif);

    @Query("SELECT sum(kd.e1) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE1 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e2) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE2 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e3) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE3 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e4) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE4 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e5) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE5 (@Param("jadwal") Jadwal jadwal);

    List<KrsDetail> findByMahasiswaNotInAndJadwalAndStatus(List<Mahasiswa> mahasiswas, Jadwal jadwal,StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.StudentDto (kd.mahasiswa.nim,kd.mahasiswa.nama,kd.mahasiswa.idProdi.namaProdi) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF' order by kd.mahasiswa.nim asc")
    List<StudentDto> cariJadwalMahasiswa(@Param("jadwal") Jadwal jadwal);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND a.nilai_akhir IS NOT NULL AND a.id_mahasiswa IS NOT NULL AND id_mahasiswa=?1" , nativeQuery = true)
    IpkDto ipk (Mahasiswa mahasiswa);
    
    @Query(value = "select round(sum(bobot*jumlah_sks)/sum(jumlah_sks),2) from (select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,max(nilai_akhir),bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester) t1" , nativeQuery = true)
    BigDecimal ipkTranskript (Mahasiswa mahasiswa);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*d.jumlah_sks)/SUM(d.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN krs AS b ON a.id_krs=b.id INNER JOIN jadwal AS c ON a.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum=d.id WHERE b.id_mahasiswa=?1 AND b.id_tahun_akademik=?2 and a.status = 'AKTIF' AND d.jumlah_sks > 0", nativeQuery = true)
    IpkDto ip (Mahasiswa mahasiswa, TahunAkademik tahun);


    KrsDetail findByJadwalAndStatusAndKrs(Jadwal j, StatusRecord aktif, Krs krs);

    Long countByJadwalAndStatus(Jadwal jadwal, StatusRecord statusRecord);

    @Query(value = "select a.* from (select a.id,a.id_mahasiswa,b.nim,b.nama,c.nama_prodi from krs as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF')a left join (select a.id,a.id_krs from krs_detail as a inner join krs as b on a.id_krs=b.id where id_jadwal=?2 and a.status='AKTIF' and b.id_tahun_akademik=?1 and b.status='AKTIF')b on a.id=b.id_krs  where b.id is null",nativeQuery = true)
    List<Object[]>cariMahasiswaJadwal(TahunAkademik tahunAkademik, Jadwal jadwal);

    @Query(value = "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkrip(Mahasiswa mahasiswa);

    @Query(value = "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 AND b.semester =?2 ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkripSem(Mahasiswa mahasiswa, String semester);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='1' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='1' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem1(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='2' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='2' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem2(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='3' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='3' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem3(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='4' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='4' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem4(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='5' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='5' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem5(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='6' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='6' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem6(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='7' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='7' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem7(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='8' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='8' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem8(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhir(Mahasiswa mahasiswa);

    @Query(value = "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 AND b.semester =?2 and bobot is not null ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkripSemesterWithoutWaiting(Mahasiswa mahasiswa, String semester);

    @Query(value = "SELECT c.nim,c.nama,d.nama_prodi,a.id_tahun_akademik,'A' AS STATUS,coalesce(e.sks_total,0) AS sks_semester,coalesce(e.ipk,0) AS ip_semester,b.sks_total,b.ipk FROM (SELECT a.* FROM krs as a inner join mahasiswa as b on a.id_mahasiswa=b.id WHERE a.STATUS='AKTIF' AND a.id_tahun_akademik=?1 and b.angkatan=?2 )a  LEFT JOIN (SELECT id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND b.jumlah_sks > 0 AND a.nilai_akhir IS NOT NULL AND a.id_mahasiswa IS NOT NULL GROUP BY a.id_mahasiswa)b ON a.id_mahasiswa=b.id_mahasiswa INNER JOIN mahasiswa AS c ON a.id_mahasiswa = c.id INNER JOIN prodi AS d ON c.id_prodi=d.id LEFT JOIN (SELECT id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b  ON a.id_matakuliah_kurikulum=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id WHERE a.status='AKTIF' AND id_tahun_akademik=?1 AND b.jumlah_sks > 0 AND a.id_mahasiswa IS NOT NULL and c.angkatan=?2 GROUP BY a.id_mahasiswa)e ON a.id_mahasiswa=e.id_mahasiswa ORDER BY d.kode_prodi, c.nim", nativeQuery = true)
    List<Object[]> cariIpk(TahunAkademik tahunAkademik,String angkatan);

    @Query(value = "SELECT sum(b.jumlah_sks) FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    Long totalSks(Mahasiswa mahasiswa);
    
    @Query(value = "select sum(jumlah_sks) from (select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,max(nilai_akhir),bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester) t1", nativeQuery = true)
    Long totalSksAkhir(Mahasiswa mahasiswa);

    @Query(value = "SELECT sum(b.jumlah_sks*a.bobot) FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    Long totalMutu(Mahasiswa mahasiswa);

    @Query(value = "select sum(bobot*jumlah_sks) from (select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,max(nilai_akhir),bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a  inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester) t1", nativeQuery = true)
    Long totalMutuAkhir(Mahasiswa mahasiswa);

    @Query(value = "select d.nim,d.nama,e.nama_prodi,f.nama_tahun_akademik,c.nama_matakuliah,a.kode_uts from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id inner join mahasiswa as d on a.id_mahasiswa=d.id inner join prodi as e on d.id_prodi=e.id inner join tahun_akademik as f on a.id_tahun_akademik=f.id left join (select count(a.id)as presensi,id_krs_detail from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah=b.id inner join presensi_dosen as c on b.id_presensi_dosen=c.id inner join jadwal as d on c.id_jadwal=d.id where a.id_mahasiswa=?1 and d.id_tahun_akademik=?2 and a.status_presensi in ('MANGKIR','TERLAMBAT') and a.status='AKTIF' group by d.id) g on a.id=g.id_krs_detail where a.id_mahasiswa=?1 and a.id_tahun_akademik=?2 and coalesce(presensi,0) < 4", nativeQuery = true)
    List<Object[]> listKartu(Mahasiswa mahasiswa,TahunAkademik tahunAkademik);

    @Query(value = "SELECT id_mahasiswa as nim,nama,kode_uas as kode FROM (SELECT a.*,d.nama,COALESCE(b.mangkir,0) AS mangkir,c.fitur FROM (SELECT * FROM krs_detail WHERE id_jadwal=?1 AND STATUS='AKTIF')a LEFT JOIN (SELECT COUNT(aa.id)AS mangkir,aa.id AS id_krs FROM presensi_mahasiswa AS aa INNER JOIN sesi_kuliah AS bb ON aa.id_sesi_kuliah=bb.id  INNER JOIN presensi_dosen AS cc ON bb.id_presensi_dosen=cc.id WHERE aa.status='AKTIF' AND aa.status_presensi IN ('MANGKIR','TERLAMBAT') GROUP BY aa.id)b  ON a.id= b.id_krs LEFT JOIN (SELECT * FROM enable_fiture WHERE ENABLE='1' AND fitur='UAS' AND id_tahun_akademik =?2)c ON a.id_mahasiswa = c.id_mahasiswa INNER JOIN mahasiswa AS d ON a.id_mahasiswa=d.id)aaa WHERE mangkir < 4 AND fitur='UAS' ORDER BY id_mahasiswa" , nativeQuery = true)
    List<Object[]> absenUas(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "SELECT id_mahasiswa as nim,nama,kode_uts as kode FROM (SELECT a.*,d.nama,COALESCE(b.mangkir,0) AS mangkir,c.fitur FROM (SELECT * FROM krs_detail WHERE id_jadwal=?1 AND STATUS='AKTIF')a LEFT JOIN (SELECT COUNT(aa.id)AS mangkir,aa.id AS id_krs FROM presensi_mahasiswa AS aa INNER JOIN sesi_kuliah AS bb ON aa.id_sesi_kuliah=bb.id  INNER JOIN presensi_dosen AS cc ON bb.id_presensi_dosen=cc.id WHERE aa.status='AKTIF' AND aa.status_presensi IN ('MANGKIR','TERLAMBAT') GROUP BY aa.id)b  ON a.id= b.id_krs LEFT JOIN (SELECT * FROM enable_fiture WHERE ENABLE='1' AND fitur='UAS' AND id_tahun_akademik =?2)c ON a.id_mahasiswa = c.id_mahasiswa INNER JOIN mahasiswa AS d ON a.id_mahasiswa=d.id)aaa WHERE mangkir < 4 AND fitur='UAS' ORDER BY id_mahasiswa" , nativeQuery = true)
    List<Object[]> absenUts(Jadwal jadwal, TahunAkademik tahunAkademik);

}
