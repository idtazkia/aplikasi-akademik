package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.ListJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.MatkulKonversiDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.SoalDto;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalIntDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.SesiDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal, String> {
    @Query("select sum (j.matakuliahKurikulum.jumlahSks)from Jadwal j where j.id in (:id)")
    Long totalSks(@Param("id")String[] id);

    Jadwal findByStatusAndId(StatusRecord statusRecord, String id);

    Jadwal findByStatusAndIdNumberElearningAndId(StatusRecord statusRecord, String idNumberElearning, String idJadwal);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status = :id and j.tahunAkademik= :tahun and j.hari= :hari")
    List<ScheduleDto> schedule(@Param("prodi") Prodi prodi, @Param("id") StatusRecord statusRecord, @Param("tahun") TahunAkademik t, @Param("hari") Hari hari);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish,j.kelas.namaKelas,j.matakuliahKurikulum.jumlahSks,j.dosen.karyawan.namaKaryawan,j.dosen.id,j.kelas.id) from Jadwal j where j.status = 'AKTIF' and j.tahunAkademik= :akademik and j.prodi = :prodi and j.hari is null and j.jamMulai is null and j.jamSelesai is null and j.kelas is not null")
    List<PlotingDto> ploting(@Param("prodi") Prodi prodi,@Param("akademik")TahunAkademik tahunAkademik);

    @Query(value = "SELECT a.sesi as sesi FROM (SELECT * FROM sesi)AS a LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_hari=?2 AND id_tahun_akademik=?1 AND id_ruangan=?3 AND STATUS='AKTIF')AS b ON a.sesi=b.sesi LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_kelas=?4 and id_tahun_akademik=?1 AND id_hari=?2 AND STATUS='AKTIF')AS c ON a.sesi=c.sesi LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_dosen_pengampu=?5 and id_tahun_akademik=?1 AND id_hari=?2 AND STATUS='AKTIF')AS d ON a.sesi=d.sesi WHERE b.id IS NULL AND c.id IS NULL group by a.sesi", nativeQuery = true)
    List<SesiDto> cariSesi(TahunAkademik tahunAkademik, Hari hari, Ruangan ruangan, Kelas kelas, Dosen dosen);

    @Query("select j from Jadwal j where j.id not in (:id) and j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan and j.sesi= :sesi and j.status= :status")
    List<Jadwal> cariJadwal(@Param("id") List<String> id, @Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("sesi")String sesi,@Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> assesment(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.dosen = :dosen and j.status =:id and j.tahunAkademik= :tahun order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> lecturerAssesment(@Param("dosen") Dosen dosen, @Param("id") StatusRecord statusRecord, @Param("tahun") TahunAkademik t);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun and j.matakuliahKurikulum.matakuliah.namaMatakuliah like %:search% order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> assesmentSearch(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t,@Param("search")String search);

    @Query(value = "SELECT a.id AS krs, a.id_mahasiswa AS mahasiswa, b.nama,  b.nim ,COALESCE(absensi_mahasiswa,0) AS absmahasiswa ,COALESCE(absen_dosen,0) AS absdosen, COALESCE(ROUND(((absensi_mahasiswa * 100)/absen_dosen),2), 0) AS absen, COALESCE(ROUND(((((absensi_mahasiswa * 100)/absen_dosen)*f.bobot_presensi)/100),2),0) AS nilaiabsen, ROUND((COALESCE(e.sds,0) * 100)/(COALESCE(g.sds,0)*10),2) AS sds,a.nilai_uts AS uts,a.nilai_uas AS uas,a.nilai_akhir AS akhir,a.grade AS grade FROM krs_detail AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id  LEFT JOIN(SELECT COUNT(a.id)AS absensi_mahasiswa,id_krs_detail FROM presensi_mahasiswa AS a INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah=b.id INNER JOIN  presensi_dosen AS c ON b.id_presensi_dosen=c.id WHERE a.status_presensi NOT IN ('MANGKIR','TERLAMBAT') AND c.id_jadwal=?1 AND a.status='AKTIF' GROUP BY id_krs_detail) AS c ON a.id=c.id_krs_detail LEFT JOIN(SELECT COUNT(id)AS absen_dosen,id_jadwal FROM presensi_dosen WHERE id_jadwal=?1 AND STATUS='AKTIF') d ON a.id_jadwal=d.id_jadwal LEFT JOIN(SELECT COUNT(b.id_mahasiswa)AS sds,b.id_mahasiswa,d.sds AS bobotsds FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id INNER JOIN jadwal AS c ON b.id_jadwal=c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum = d.id INNER JOIN matakuliah AS e ON d.id_matakuliah = e.id WHERE LEFT(e.kode_matakuliah,3)='SDS' AND a.status='AKTIF' AND a.status_presensi NOT IN ('MANGKIR','TERLAMBAT') AND c.id_tahun_akademik=?2 GROUP BY a.id_mahasiswa)e ON a.id_mahasiswa=e.id_mahasiswa INNER JOIN jadwal AS f ON a.id_jadwal = f.id INNER JOIN matakuliah_kurikulum AS g ON f.id_matakuliah_kurikulum=g.id WHERE a.id_jadwal=?1 AND a.status='AKTIF'", nativeQuery = true)
    List<ScoreDto>  scoreInput(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "SELECT id_krs_detail as krs, id_mahasiswa, aa.nama, nim, absensi_mahasiswa, absen_dosen, absen, nilai_absen, nilai_sds, nilai_tugas, nilai_uts, nilai_uas, nilai_akhir as nilaiakhir, bb.nama AS grade, bb.bobot as bobot FROM (SELECT a.id AS id_krs_Detail, a.id_mahasiswa, b.nama, b.nim, COALESCE(absensi_mahasiswa,0) as absensi_mahasiswa, COALESCE(absen_dosen,0)as absen_dosen, ROUND((((COALESCE(absensi_mahasiswa,0)) * 100) / COALESCE(absen_dosen,1)), 2) AS absen, ROUND(((((COALESCE(absensi_mahasiswa,0) * 100) / COALESCE(absen_dosen,1)) * COALESCE(f.bobot_presensi,0)) / 100), 2) AS nilai_absen, ROUND(((COALESCE(sds, 0) * 10 * COALESCE(sdsb, 0)) / 100), 2) AS nilai_sds, g.nilai_tugas, a.nilai_uts, a.nilai_uas, g.nilai_tugas + ((COALESCE(sds,0) * 10 * COALESCE(sdsb,0)) / 100) + ((((COALESCE(absensi_mahasiswa,1) * 100) / COALESCE(absen_dosen,1)) * COALESCE(f.bobot_presensi,0)) / 100) + (a.nilai_uts * f.bobot_uts / 100) + (a.nilai_uas * (bobot_uas / 100)) AS nilai_akhir, a.grade FROM krs_detail AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa = b.id LEFT JOIN (SELECT COUNT(a.id) AS absensi_mahasiswa, id_krs_detail FROM presensi_mahasiswa AS a INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah = b.id INNER JOIN presensi_dosen AS c ON b.id_presensi_dosen = c.id WHERE a.status_presensi NOT IN ('MANGKIR' , 'TERLAMBAT') AND c.id_jadwal =?1 AND a.status = 'AKTIF' GROUP BY id_krs_detail) AS c ON a.id = c.id_krs_detail LEFT JOIN (SELECT COUNT(id) AS absen_dosen, id_jadwal FROM presensi_dosen WHERE id_jadwal =?1 AND STATUS = 'AKTIF') d ON a.id_jadwal = d.id_jadwal LEFT JOIN (SELECT COUNT(b.id_mahasiswa) AS sds, b.id_mahasiswa, d.sds AS bobotsds FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail = b.id INNER JOIN jadwal AS c ON b.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum = d.id INNER JOIN matakuliah AS e ON d.id_matakuliah = e.id WHERE LEFT(e.kode_matakuliah, 3) = 'SDS' AND a.status = 'AKTIF' AND a.status_presensi NOT IN ('MANGKIR' , 'TERLAMBAT') AND c.id_tahun_akademik = '20191' GROUP BY a.id_mahasiswa) e ON a.id_mahasiswa = e.id_mahasiswa INNER JOIN (SELECT a.*, sds AS sdsb FROM jadwal AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id) f ON a.id_jadwal = f.id LEFT JOIN (SELECT a.id_krs_detail, ROUND(SUM(a.nilai * c.bobot / 100), 2) AS nilai_tugas FROM krs_nilai_tugas AS a INNER JOIN krs_detail AS b ON a.id_krs_detail = b.id INNER JOIN jadwal_bobot_tugas AS c ON a.id_bobot_tugas = c.id WHERE b.id_jadwal =?1 AND a.status = 'AKTIF' AND c.status = 'AKTIF' GROUP BY a.id_krs_detail) g ON a.id = g.id_krs_detail WHERE a.id_jadwal =?1 AND a.status = 'AKTIF') AS aa LEFT JOIN grade AS bb ON aa.nilai_akhir >= bawah AND nilai_akhir < atas", nativeQuery = true)
    List<ScoreHitungDto> hitungUploadScore(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "SELECT a.*  FROM (SELECT a.id,a.id_mahasiswa,b.nim,b.nama,c.nama_prodi FROM krs AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id INNER JOIN prodi AS c ON a.id_prodi=c.id WHERE a.id_tahun_akademik=?2 AND a.status='AKTIF' AND a.id_prodi=?1 GROUP BY id_mahasiswa)a LEFT JOIN (SELECT a.* FROM krs AS a INNER JOIN krs_detail AS b ON a.id=b.id_krs INNER JOIN jadwal AS c ON b.id_jadwal=c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum=d.id  INNER JOIN (SELECT * FROM matakuliah WHERE LEFT(kode_matakuliah,3)='SDS') e ON d.id_matakuliah=e.id WHERE a.id_tahun_akademik=?2 AND a.status='AKTIF' AND a.id_prodi=?1 GROUP BY a.id)b ON a.id=b.id WHERE b.id IS NULL ORDER BY a.nim",nativeQuery = true)
    List<Object[]> cariMahasiswaBelumSds(Prodi prodi, TahunAkademik tahunAkademik);

    @Query(value = "select bobot_uts + bobot_uas as bobot from jadwal where id=?1 and status='AKTIF'", nativeQuery = true)
    BigDecimal bobotUtsUas(String idJadwal);


    @Query(value = "select count(id) as jmljadwal from jadwal where id_kelas = ?1 and status = 'AKTIF'", nativeQuery = true)
    Integer jmlJadwal(String idJadwal);


    @Query(value = "SELECT b.*,d.nama_matakuliah,d.nama_matakuliah_english,e.nama_kelas,g.nama_karyawan FROM jadwal_dosen AS a\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "INNER JOIN kelas AS e ON b.id_kelas = e.id\n" +
            "INNER JOIN dosen AS f ON a.id_dosen = f.id\n" +
            "INNER JOIN karyawan AS g ON f.id_karyawan = g.id\n" +
            "WHERE a.status_jadwal_dosen = 'PENGAMPU' AND b.status = 'AKTIF' AND a.id_dosen = ?1\n" +
            "AND b.sesi = ?2 AND b.id_tahun_akademik = ?3\n" +
            "AND b.id_hari = ?4 and b.id <> ?5", nativeQuery = true)
    List<Object[]> cariJadwalAKtifDosen(String idDosen, String idSesi, String idTahunAKademik, String idHari, String idJadwal);

    @Query(value = "SELECT COUNT(b.id)AS jml FROM jadwal_dosen AS a\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "INNER JOIN kelas AS e ON b.id_kelas = e.id\n" +
            "WHERE a.status_jadwal_dosen = 'PENGAMPU' AND b.status = 'AKTIF' AND a.id_dosen = ?1\n" +
            "AND b.sesi = ?2 AND b.id_tahun_akademik = ?3\n" +
            "AND b.id_hari = ?4 AND id_jadwal <> ?5", nativeQuery = true)
    Integer jumlahBentrok(String idDosen, String idSesi, String idTahunAKademik, String idHari, String idJadwal);



    List<Jadwal> findByStatusAndTahunAkademikAndDosenAndHariNotNull(StatusRecord aktif, TahunAkademik tahun, Dosen dosen);

    List<Jadwal> findByStatusAndTahunAkademikAndKelasAndHariNotNull(StatusRecord aktif, TahunAkademik byStatus, Kelas kelas);

    List<Jadwal> findByStatusAndTahunAkademikAndHariAndRuangan(StatusRecord aktif, TahunAkademik tahunAkademik, Hari hari, Ruangan ruang1);

    @Query(value = "select a.id as id, c.nama_matakuliah as namaMatakuliah, c.nama_matakuliah_english as namaMatakuliahEnglish, c.kode_matakuliah as kodeMatakuliah, f.nama_karyawan as dosen from\n" +
            "(select * from jadwal where id_tahun_akademik = ?1 and id_prodi = ?2 and status = 'AKTIF' and id_hari is not null and jam_mulai is not null\n" +
            "union\n" +
            "select * from jadwal where id_tahun_akademik = ?1 and id_prodi <> ?2 and akses = 'UMUM' and status = 'AKTIF' and id_hari is not null and jam_mulai is not null)a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "inner join matakuliah as c on b.id_matakuliah= c.id\n" +
            "inner join prodi as d on a.id_prodi = d.id\n" +
            "inner join dosen as e on a.id_dosen_pengampu = e.id\n" +
            "inner join karyawan as f on e.id_karyawan = f.id \n" +
            "where d.id_jenjang = ?3 and b.jumlah_sks > 0\n" +
            "order by nama_matakuliah", nativeQuery = true)
    List<MatkulKonversiDto> cariMatkulKonversi(String idTahun, String idProdi, String idJenjang);

    List<Jadwal> findByTahunAkademikAndProdiAndHariNotNull(TahunAkademik tahunAkademik, Prodi prodi);

    Page<Jadwal> findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, String search,StatusRecord status, TahunAkademik akademik, String search1, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUasAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, StatusApprove status, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUasAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndStatusUasAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, StatusApprove status, String search, StatusRecord statusAktif,StatusApprove statusApprove, TahunAkademik tahunAkademik,String search1, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUtsAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, StatusApprove status, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUtsAndDosenKaryawanNamaKaryawanContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNullOrStatusAndStatusUtsAndTahunAkademikAndMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, StatusApprove status, String search, StatusRecord statusAktif,StatusApprove statusApprove, TahunAkademik tahunAkademik,String search1, Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord aktif, TahunAkademik tahun, String search, String search1, Pageable page);

    @Query(value = "SELECT id_krs_detail as krs, id_mahasiswa, aa.nama, nim, absensi_mahasiswa, absen_dosen, absen, nilai_absen, nilai_sds, nilai_tugas, nilai_uts, nilai_uas, nilai_akhir as nilaiakhir, bb.nama AS grade, bb.bobot as bobot FROM (SELECT a.id AS id_krs_Detail, a.id_mahasiswa, b.nama, b.nim, COALESCE(absensi_mahasiswa,0) as absensi_mahasiswa, COALESCE(absen_dosen,0)as absen_dosen, ROUND((((COALESCE(absensi_mahasiswa,0)) * 100) / COALESCE(absen_dosen,1)), 2) AS absen, ROUND(((((COALESCE(absensi_mahasiswa,0) * 100) / COALESCE(absen_dosen,1)) * COALESCE(f.bobot_presensi,0)) / 100), 2) AS nilai_absen, ROUND(((COALESCE(sds, 0) * 10 * COALESCE(sdsb, 0)) / 100), 2) AS nilai_sds, g.nilai_tugas, a.nilai_uts, a.nilai_uas, g.nilai_tugas + ((COALESCE(sds,0) * 10 * COALESCE(sdsb,0)) / 100) + ((((COALESCE(absensi_mahasiswa,1) * 100) / COALESCE(absen_dosen,1)) * COALESCE(f.bobot_presensi,0)) / 100) + (a.nilai_uts * f.bobot_uts / 100) + (a.nilai_uas * (bobot_uas / 100)) AS nilai_akhir, a.grade FROM krs_detail AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa = b.id LEFT JOIN (SELECT COUNT(a.id) AS absensi_mahasiswa, id_krs_detail FROM presensi_mahasiswa AS a INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah = b.id INNER JOIN presensi_dosen AS c ON b.id_presensi_dosen = c.id WHERE a.status_presensi NOT IN ('MANGKIR' , 'TERLAMBAT') AND c.id_jadwal =?1 AND a.status = 'AKTIF' GROUP BY id_krs_detail) AS c ON a.id = c.id_krs_detail LEFT JOIN (SELECT COUNT(id) AS absen_dosen, id_jadwal FROM presensi_dosen WHERE id_jadwal =?1 AND STATUS = 'AKTIF') d ON a.id_jadwal = d.id_jadwal LEFT JOIN (SELECT COUNT(b.id_mahasiswa) AS sds, b.id_mahasiswa, d.sds AS bobotsds FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail = b.id INNER JOIN jadwal AS c ON b.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum = d.id INNER JOIN matakuliah AS e ON d.id_matakuliah = e.id WHERE LEFT(e.kode_matakuliah, 3) = 'SDS' AND a.status = 'AKTIF' AND a.status_presensi NOT IN ('MANGKIR' , 'TERLAMBAT') AND c.id_tahun_akademik = '6cf08aa7-fc62-40f3-b82d-ff04be2c8905' GROUP BY a.id_mahasiswa) e ON a.id_mahasiswa = e.id_mahasiswa INNER JOIN (SELECT a.*, sds AS sdsb FROM jadwal AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id) f ON a.id_jadwal = f.id LEFT JOIN (SELECT a.id_krs_detail, ROUND(SUM(a.nilai * c.bobot / 100), 2) AS nilai_tugas FROM krs_nilai_tugas AS a INNER JOIN krs_detail AS b ON a.id_krs_detail = b.id INNER JOIN jadwal_bobot_tugas AS c ON a.id_bobot_tugas = c.id WHERE b.id_jadwal =?1 AND a.status = 'AKTIF' AND c.status = 'AKTIF' GROUP BY a.id_krs_detail) g ON a.id = g.id_krs_detail WHERE a.id_jadwal =?1 AND a.status = 'AKTIF') AS aa LEFT JOIN grade AS bb ON aa.nilai_akhir >= bawah AND nilai_akhir < atas", nativeQuery = true)
    List<ScoreHitungDto> hitungUploadScore2(Jadwal jadwal, TahunAkademik tahunAkademik);

    List<Jadwal> findByStatusAndTahunAkademik(StatusRecord statusRecord, TahunAkademik tahunaAkademik);

    List<Jadwal> findByMatakuliahKurikulumAndStatus(MatakuliahKurikulum matkur, StatusRecord statusRecord);

    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null", nativeQuery = true)
    List<ListJadwalDto> listJadwalDto( String idProdi, String idTahunAkademik);

    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 and id_number_elearning = ?3 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null", nativeQuery = true)
    List<ListJadwalDto> byJadwal1( String idProdi, String idTahunAkademik, String idNumber);

    Jadwal findByIdNumberElearningAndTahunAkademikAndStatus(String idNumberElearning, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    List<Jadwal> findByTahunAkademikAndIdNumberElearningAndStatus(TahunAkademik tahunAkademik,String idNumberElearning, StatusRecord statusRecord);

    @Query(value = "select a.id from jadwal as a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "where id_tahun_akademik = ?1 and b.sds is not null and a.id_prodi = ?2 and a.status = 'AKTIF' and b.sds > 0\n" +
            "and id_hari is not null", nativeQuery = true)
    List<String> findSds1(String idTahunAkademik, String idProdi);

    @Query(value = "select a.id from jadwal as a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "where a.id =  ?1 and a.status = 'AKTIF' and b.sds > 0\n" +
            "and id_hari is not null", nativeQuery = true)
    String findSds2(String idJadwal);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish,j.kelas.namaKelas,j.matakuliahKurikulum.jumlahSks,j.dosen.karyawan.namaKaryawan,j.dosen.id,j.kelas.id) from Jadwal j where j.status = 'AKTIF' and j.tahunAkademik= :akademik  and j.hari is null and j.jamMulai is null and j.jamSelesai is null and j.kelas is not null")
    List<PlotingDto> listPloting(@Param("akademik")TahunAkademik tahunAkademik);

    List<Jadwal> findByTahunAkademikAndDosenAndStatus(TahunAkademik tahunAkademik, Dosen dosen, StatusRecord statusRecord);

    @Query(value = "select j.id,j.id_dosen_pengampu as idDos,j.akses_uts as akses ,r.nama_ruangan as ruangan,h.nama_hari as hari,j.jam_mulai as mulai,j.jam_selesai as selesai,m.nama_matakuliah as matkul,ke.nama_kelas as kelas,k.nama_karyawan as nama,j.status_uts as status,ss.id as soal from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id inner join kelas as ke on j.id_kelas = ke.id inner join hari as h on j.id_hari = h.id inner join ruangan as r on j.id_ruangan = r.id left join (select s.id as id, s.id_jadwal as jad from soal as s where status = 'AKTIF' and status_approve = 'APPROVED' and status_soal = 'UTS') ss on j.id = ss.jad where j.id_tahun_akademik= ?1 and j.status='AKTIF' and j.id_hari is not null and j.jam_mulai is not null and j.jam_selesai is not null and (j.id_dosen_pengampu = ?2 or j.akses_uts=?2);", nativeQuery = true)
    List<SoalDto> listUts(TahunAkademik tahunAkademik, Dosen dosen);

    @Query(value = "select j.id,j.id_dosen_pengampu as idDos,j.akses_uas as akses ,r.nama_ruangan as ruangan,h.nama_hari as hari,j.jam_mulai as mulai,j.jam_selesai as selesai,m.nama_matakuliah as matkul,ke.nama_kelas as kelas,k.nama_karyawan as nama,j.status_uas as status, ss.id as soal from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id inner join kelas as ke on j.id_kelas = ke.id inner join hari as h on j.id_hari = h.id inner join ruangan as r on j.id_ruangan = r.id left join (select s.id as id, s.id_jadwal as jad from soal as s where status = 'AKTIF' and status_approve = 'APPROVED' and status_soal = 'UAS') ss on j.id = ss.jad where j.id_tahun_akademik= ?1 and j.status='AKTIF' and j.id_hari is not null and j.jam_mulai is not null and j.jam_selesai is not null and (j.id_dosen_pengampu = ?2 or j.akses_uas=?2)", nativeQuery = true)
    List<SoalDto> listUas(TahunAkademik tahunAkademik, Dosen dosen);

    Jadwal findByIdNumberElearning(String idNumber);

    @Query(value = "select a.id as id_jadwal, id_dosen, id_kelas, coalesce(b.nama_ruangan,'-')as ruangan, coalesce(jam_mulai,'-')as jam_mulai, coalesce(jam_selesai,'-') as jam_selesai, nama_prodi, kode_matakuliah, nama_matakuliah, coalesce(jumlah_sks,0) as jumlah_sks, nama_kelas, nama_karyawan, coalesce(c.nama_hari,'-')as hari,total_sks_dosen, coalesce(total_mahasiswa,0) as total_mahasiswa from (select a.*, d.id as id_dosen, c.kode_matakuliah, c.nama_matakuliah, c.nama_matakuliah_english, b.jumlah_sks, f.nama_kelas, e.nama_karyawan, g.nama_prodi from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah = c.id inner join dosen as d on a.id_dosen_pengampu = d.id inner join karyawan as e on d.id_karyawan = e.id inner join kelas as f on a.id_kelas =f.id inner join prodi as g on a.id_prodi=g.id where a.id_tahun_akademik = ?1 and a.status = 'AKTIF')as a left join ruangan as b on a.id_ruangan = b.id left join hari as c on a.id_hari = c.id left join (select id_dosen_pengampu,sum(jumlah_sks) as total_sks_dosen from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id where a.status = 'AKTIF' and a.id_tahun_akademik = ?1 group by id_dosen_pengampu)d on a.id_dosen_pengampu = d.id_dosen_pengampu left join (select b.id as id_jadwal, count(id_mahasiswa)as total_mahasiswa from krs_detail as a inner join jadwal as b on a.id_jadwal = b.id where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' group by a.id_jadwal) as e on a.id = e.id_jadwal order by nama_prodi, nama_kelas", nativeQuery = true)
    List<Object[]> downloadJadwal(TahunAkademik tahunAkademik);

    @Query(value = "select a.id as id,d.nama_prodi as namaProdi, e.nama_kelas as namaKelas, c.kode_matakuliah as kodeMatakuliah, c.nama_matakuliah as namaMatakuliah, c.nama_matakuliah_english as namaMatakuliahEnglish, g.id as idDosen, h.nama_karyawan as dosen,jam_mulai as jamMulai, jam_selesai as jamSelesai, a.id_number_elearning as idNumberElearning, a.id_tahun_akademik as idTahunAkademik, a.status as status\n" +
            "from jadwal as a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id\n" +
            "inner join prodi as d on a.id_prodi = d.id\n" +
            "inner join kelas as e on a.id_kelas = e.id\n" +
            "inner join jadwal_dosen as f on a.id = f.id_jadwal\n" +
            "inner join dosen as g on f.id_dosen = g.id\n" +
            "inner join karyawan as h on g.id_karyawan = h.id\n" +
            "inner join tahun_akademik as i on a.id_tahun_akademik = i.id\n" +
            "where i.status = 'AKTIF'",nativeQuery = true)
    List<DetailJadwalIntDto> getDetailJadwal();

    @Modifying
    @Query(value = "update jadwal set final_status='FINAL' where id_tahun_akademik=?1 and status='AKTIF' and id_hari is not null and jam_mulai is not null and jam_selesai is not null", nativeQuery = true)
    void updateFinalStatus(String idTahunAkademik);

}
