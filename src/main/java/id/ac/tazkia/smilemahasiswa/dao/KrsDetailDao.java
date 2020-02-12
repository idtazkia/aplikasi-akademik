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

    @Query(value = "select aaa.id,ddd.nama_hari,ggg.nama_kelas,aaa.jam_mulai,aaa.jam_selesai,bbb.jumlah_sks,fff.nama_karyawan as dosen,ccc.nama_matakuliah,kapasitas,mhsw,cmkk,bmkk,aaa.kode_matakuliah as matkul_prass from (select aa.*,bb.kode_matakuliah as bmkk,cc.kode_matakuliah as cmkk from (select a.*,b.id_matakuliah_kurikulum_pras,b.id_matakuliah_pras,b.nilai,d.id as id_matkul_pras,d.kode_matakuliah from jadwal as a  left join prasyarat as b on a.id_matakuliah_kurikulum=b.id_matakuliah_kurikulum inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum=c.id left join matakuliah as d on b.id_matakuliah_pras=d.id where a.status='AKTIF' and a.id_tahun_akademik=?1 and a.akses='TERTUTUP' and a.id_kelas=?2 and id_hari is not null union select a.*,b.id_matakuliah_kurikulum_pras,b.id_matakuliah_pras,b.nilai,d.id as id_matkul_pras,d.kode_matakuliah from jadwal as a left join prasyarat as b on a.id_matakuliah_kurikulum=b.id_matakuliah_kurikulum left join matakuliah as d on b.id_matakuliah_pras=d.id where a.status='AKTIF' and a.id_tahun_akademik=?1 and a.akses='PRODI' and a.id_prodi=?3 and id_hari is not null union select a.*,b.id_matakuliah_kurikulum_pras,b.id_matakuliah_pras,b.nilai,d.id as id_matkul_pras,d.kode_matakuliah from jadwal as a left join prasyarat as b on a.id_matakuliah_kurikulum=b.id_matakuliah_kurikulum left join matakuliah as d on b.id_matakuliah_pras=d.id where a.status='AKTIF' and a.id_tahun_akademik=?1 and a.akses='UMUM' and a.id_hari is not null)aa left join (select a.*,c.id_matakuliah_pras, c.nilai,d.id as id_matkul_pras,d.kode_matakuliah from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id  left join prasyarat as c on b.id_matakuliah=c.id_matakuliah_pras inner join matakuliah as d on c.id_matakuliah_pras=d.id  where a.status='AKTIF' and id_mahasiswa=?4 and a.bobot > c.nilai group by a.id) bb on (aa.id_matakuliah_pras=bb.id_matakuliah_pras or aa.id_matkul_pras=bb.id_matkul_pras or aa.kode_matakuliah=bb.kode_matakuliah) and aa.nilai=bb.nilai  left join (select x.*,z.kode_matakuliah from krs_detail as x inner join matakuliah_kurikulum as y on x.id_matakuliah_kurikulum=y.id inner join matakuliah as z on y.id_matakuliah=z.id where x.status='AKTIF' and id_mahasiswa=?4 and bobot >= 3.00) cc on aa.id_matakuliah_kurikulum = cc.id_matakuliah_kurikulum)aaa inner join matakuliah_kurikulum as bbb on aaa.id_matakuliah_kurikulum = bbb.id inner join matakuliah as ccc on bbb.id_matakuliah=ccc.id inner join hari as ddd on aaa.id_hari=ddd.id inner join dosen as eee on aaa.id_dosen_pengampu=eee.id inner join karyawan as fff on eee.id_karyawan=fff.id inner join kelas as ggg on aaa.id_kelas=ggg.id left join (select xx.id as krs_detail, id_jadwal from krs_detail as xx inner join krs as yy on xx.id_krs=yy.id where xx.status='AKTIF' and yy.status='AKTIF' and yy.id_tahun_akademik=?1 and yy.id_mahasiswa=?4)hhh  on aaa.id = hhh.id_jadwal left join(select id_jadwal,count(a.id)as mhsw from krs_detail as a inner join krs as  b on a.id_krs=b.id where a.status='AKTIF' and b.id_tahun_akademik=?1  group by id_jadwal)iii on aaa.id=iii.id_jadwal where cmkk is null and coalesce(bmkk,'AA') = coalesce(aaa.kode_matakuliah,'AA')  and krs_detail is null and kapasitas > coalesce(mhsw,0)  group by aaa.id", nativeQuery = true)
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

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.namaMatakuliah = :nama and kd.nilaiAkhir > 60")
    List<BigDecimal> nilaiMagang(@Param("status")StatusRecord statusRecord, @Param("mahasiswa") Mahasiswa mahasiswa, @Param("nama")String nama);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.singkatan = :singkatan and kd.nilaiAkhir > 60")
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

    @Query("select kd.id from KrsDetail kd where kd.mahasiswa.id = :id and kd.status = :status and kd.matakuliahKurikulum.matakuliah.namaMatakuliah like %:nama% and kd.krs.tahunAkademik = :tahun")
    String idKrsDetail(@Param("id")String id,@Param("status")StatusRecord statusRecord,@Param("nama")String nama,@Param("tahun")TahunAkademik tahunAkademik);

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

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.StudentDto (kd.mahasiswa.nim,kd.mahasiswa.nama,kd.mahasiswa.idProdi.namaProdi) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF' order by kd.mahasiswa.nim asc")
    List<StudentDto> cariJadwalMahasiswa(@Param("jadwal") Jadwal jadwal);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*d.jumlah_sks)/SUM(d.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN krs AS b ON a.id_krs=b.id INNER JOIN jadwal AS c ON a.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum=d.id WHERE b.id_mahasiswa=?1 AND d.jumlah_sks > 0" , nativeQuery = true)
    IpkDto ipk (Mahasiswa mahasiswa);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*d.jumlah_sks)/SUM(d.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN krs AS b ON a.id_krs=b.id INNER JOIN jadwal AS c ON a.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum=d.id WHERE b.id_mahasiswa=?1 AND b.id_tahun_akademik=?2 AND d.jumlah_sks > 0", nativeQuery = true)
    IpkDto ip (Mahasiswa mahasiswa, TahunAkademik tahun);



}
