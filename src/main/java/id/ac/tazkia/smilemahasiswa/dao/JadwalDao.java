package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.ListJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.SesiDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun and j.hari= :hari")
    List<ScheduleDto> schedule(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t, @Param("hari") Hari hari);

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

    Jadwal findByTahunAkademikAndProdiAndKelasAndTahunAkademikProdiAndMatakuliahKurikulumAndStatus(TahunAkademik tahunAkademik, Prodi prodi,
                                                                                          Kelas kelas, TahunAkademikProdi tahunAkademikProdi,
                                                                                          MatakuliahKurikulum matakuliahKurikulum, StatusRecord statusRecord);


    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null", nativeQuery = true)
    List<ListJadwalDto> listJadwalDto( String idProdi, String idTahunAkademik);

}
