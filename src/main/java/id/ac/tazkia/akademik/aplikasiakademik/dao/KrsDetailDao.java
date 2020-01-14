package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapPresensi (kd.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah,kd.jadwal.matakuliahKurikulum.jumlahSks,kd.jadwal.kelas.namaKelas,kd.jadwal.dosen.karyawan.namaKaryawan,0,kd.jadwal.id)from KrsDetail kd where kd.status = :status and kd.krs =:krs and kd.mahasiswa = :mahasiswa order by kd.jadwal.hari asc,kd.jadwal.jamMulai asc ")
    List<RekapPresensi> cariPresensi(@Param("status")StatusRecord statusRecord,@Param("krs")Krs krs,@Param("mahasiswa")Mahasiswa mahasiswa);

    @Query("select kd.id from KrsDetail kd where kd.status = :status and kd.krs =:krs and kd.mahasiswa = :mahasiswa order by kd.jadwal.hari asc,kd.jadwal.jamMulai asc ")
    List<String> cariJadwalId(@Param("status")StatusRecord statusRecord,@Param("krs")Krs krs,@Param("mahasiswa")Mahasiswa mahasiswa);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.hari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.hari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);

    List<KrsDetail> findByMatakuliahKurikulumAndStatusAndMahasiswa(MatakuliahKurikulum matakuliahKurikulum,StatusRecord statusRecord,Mahasiswa mahasiswa);
    List<KrsDetail> findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(String kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    List<KrsDetail> findByMatakuliahKurikulumAndMahasiswaAndStatusAndNilaiAkhirGreaterThan(MatakuliahKurikulum kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,BigDecimal bigDecimal);
    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal jadwal, StatusRecord statusRecord, TahunAkademik tahunAkademik);

    Page<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord,Pageable page);
    List<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord);
    KrsDetail findByJadwalAndStatusAndKrs(Jadwal jadwal,StatusRecord statusRecord,Krs krs);

    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatus(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord);
    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord,StatusRecord statusEdom);
    KrsDetail findByMahasiswaAndKrsTahunAkademikAndStatusAndMatakuliahKurikulumMatakuliahNamaMatakuliahLike(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord,String matkul);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.TestDto(kd.id,kd.mahasiswa.nim,kd.mahasiswa.nama,0,kd.nilaiUts,kd.nilaiUas,0,kd.nilaiAkhir,kd.grade) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    List<TestDto> penilaianList(@Param("jadwal")String jadwal,@Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.NilaiDto(kd.mahasiswa.nim,kd.nilaiUts) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    List<NilaiDto> nilaiDto(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks,kd.matakuliahKurikulum.statusSkripsi) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status")
    Page<RekapSksDto> cariSks(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks,kd.matakuliahKurikulum.statusSkripsi) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status and kd.matakuliahKurikulum.statusSkripsi = :skripsi")
    Page<RekapSksDto> tanpaSkripsi(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord,@Param("skripsi")StatusRecord skripsi, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.AbsenDto (kd.mahasiswa.nim,kd.mahasiswa.nama,kd.kodeUas,1,0) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = :status")
    Iterable<AbsenDto> cariId (@Param("jadwal")Jadwal jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select count (kd.id) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    Long jumlahMahasiswa(@Param("jadwal")String jadwal,@Param("status")StatusRecord statusRecord);

    @Query("select kd.id from KrsDetail kd where kd.mahasiswa.id = :id and kd.status = :status and kd.matakuliahKurikulum.matakuliah.namaMatakuliah like %:nama% and kd.krs.tahunAkademik = :tahun")
    String idKrsDetail(@Param("id")String id,@Param("status")StatusRecord statusRecord,@Param("nama")String nama,@Param("tahun")TahunAkademik tahunAkademik);

    KrsDetail findByMahasiswaAndJadwalAndStatus(Mahasiswa mahasiswa, Jadwal jadwal, StatusRecord statusRecord);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.namaMatakuliah = :nama and kd.nilaiAkhir > 60")
    List<BigDecimal> nilaiMagang(@Param("status")StatusRecord statusRecord,@Param("mahasiswa") Mahasiswa mahasiswa,@Param("nama")String nama);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.singkatan = :singkatan and kd.nilaiAkhir > 60")
    List<BigDecimal> nilaiMetolit(@Param("status")StatusRecord statusRecord,@Param("mahasiswa") Mahasiswa mahasiswa,@Param("singkatan")String singkatan);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.KhsDto(kd.id,kd.matakuliahKurikulum.matakuliah.kodeMatakuliah,kd.matakuliahKurikulum.matakuliah.namaMatakuliah,kd.matakuliahKurikulum.jumlahSks,kd.nilaiPresensi,kd.nilaiTugas,kd.nilaiUts,kd.nilaiUas,kd.nilaiPresensi,kd.nilaiAkhir,kd.nilaiUts) from KrsDetail kd where kd.status = :status and kd.krs = :krs and kd.mahasiswa = :mahasiswa order by kd.jadwal.hari,kd.jadwal.jamMulai asc ")
    List<KhsDto> khsMahasiswa (@Param("status") StatusRecord status,@Param("krs") Krs krs, @Param("mahasiswa") Mahasiswa mahasiswa);

    @Query(value = "select b.id,b.id_matakuliah_kurikulum as matakuliah,b.nilai_presensi as presensi ,b.nilai_tugas as tugas,b.nilai_uts as uts,b.nilai_uas as uas,b.nilai_akhir as nilaiAkhir,c.bobot as bobot,c.nama as grade from krs as a inner join krs_detail as b on a.id = b.id_krs left join grade as c on b.nilai_akhir >= c.bawah and b.nilai_akhir <= c.atas where a.id_tahun_akademik= ?1 and a.id_mahasiswa= ?2 and a.status='AKTIF' and b.status='aktif'", nativeQuery = true)
    List<Khs> getKhs(TahunAkademik tahunAkademik,Mahasiswa mahasiswa);

    @Query("select kd.kodeUas from KrsDetail kd where kd.status = 'AKTIF' and kd.mahasiswa.nim = :mahasiswa and kd.jadwal =:jadwal")
    String cariId (@Param("mahasiswa") String nim,@Param("jadwal") Jadwal jadwal);

    @Query(value = "SELECT id_mahasiswa as nim,nama,kode_uas as kode FROM (SELECT a.*,d.nama,COALESCE(b.mangkir,0) AS mangkir,c.fitur FROM (SELECT * FROM krs_detail WHERE id_jadwal=?1 AND STATUS='AKTIF')a LEFT JOIN (SELECT COUNT(aa.id)AS mangkir,aa.id AS id_krs FROM presensi_mahasiswa AS aa INNER JOIN sesi_kuliah AS bb ON aa.id_sesi_kuliah=bb.id  INNER JOIN presensi_dosen AS cc ON bb.id_presensi_dosen=cc.id WHERE aa.status='AKTIF' AND aa.status_presensi IN ('MANGKIR','TERLAMBAT') GROUP BY aa.id)b  ON a.id= b.id_krs LEFT JOIN (SELECT * FROM enable_fiture WHERE ENABLE='1' AND fitur='UAS' AND id_tahun_akademik =?2)c ON a.id_mahasiswa = c.id_mahasiswa INNER JOIN mahasiswa AS d ON a.id_mahasiswa=d.id)aaa WHERE mangkir < 4 AND fitur='UAS' ORDER BY id_mahasiswa" , nativeQuery = true)
    List<Absen> absenUas(Jadwal jadwal, TahunAkademik tahunAkademik);


}