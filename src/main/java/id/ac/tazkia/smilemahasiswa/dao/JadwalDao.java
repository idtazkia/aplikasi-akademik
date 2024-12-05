package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.ListJadwalDto;
import id.ac.tazkia.smilemahasiswa.dto.MatkulKonversiDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreHitungDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.SoalDto;
import id.ac.tazkia.smilemahasiswa.dto.courses.DetailJadwalIntDto;
import id.ac.tazkia.smilemahasiswa.dto.ploting.DataPlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.ploting.ValidasiSesi;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapAbsenDosen;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ListPlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.SesiDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.JadwalTlc;
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

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status = :id and j.tahunAkademik= :tahun and j.hari= :hari order by j.jamMulai asc")
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

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.status =:id and j.tahunAkademik= :tahun order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> lecturerAssesmentAll(@Param("id") StatusRecord statusRecord, @Param("tahun") TahunAkademik t);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.kodeMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun and j.matakuliahKurikulum.matakuliah.namaMatakuliah like %:search% order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> assesmentSearch(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t,@Param("search")String search);

    @Query(value = "SELECT a.id AS krs, a.id_mahasiswa AS mahasiswa, b.nama,  b.nim ,COALESCE(absensi_mahasiswa,0) AS absmahasiswa ,COALESCE(absen_dosen,0) AS absdosen, COALESCE(ROUND(((absensi_mahasiswa * 100)/absen_dosen),2), 0) AS absen, COALESCE(ROUND(((((absensi_mahasiswa * 100)/absen_dosen)*f.bobot_presensi)/100),2),0) AS nilaiabsen, ROUND((COALESCE(e.sds,0) * 100)/(COALESCE(g.sds,0)*10),2) AS sds,a.nilai_uts AS uts,a.nilai_uas AS uas,a.nilai_akhir AS akhir,a.grade AS grade FROM krs_detail AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id  LEFT JOIN(SELECT COUNT(a.id)AS absensi_mahasiswa,id_krs_detail FROM presensi_mahasiswa AS a INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah=b.id INNER JOIN  presensi_dosen AS c ON b.id_presensi_dosen=c.id WHERE a.status_presensi NOT IN ('MANGKIR','TERLAMBAT') AND c.id_jadwal=?1 AND a.status='AKTIF' GROUP BY id_krs_detail) AS c ON a.id=c.id_krs_detail LEFT JOIN(SELECT COUNT(id)AS absen_dosen,id_jadwal FROM presensi_dosen WHERE id_jadwal=?1 AND STATUS='AKTIF') d ON a.id_jadwal=d.id_jadwal LEFT JOIN(SELECT COUNT(b.id_mahasiswa)AS sds,b.id_mahasiswa,d.sds AS bobotsds FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id INNER JOIN jadwal AS c ON b.id_jadwal=c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum = d.id INNER JOIN matakuliah AS e ON d.id_matakuliah = e.id WHERE LEFT(e.kode_matakuliah,3)='SDS' AND a.status='AKTIF' AND a.status_presensi NOT IN ('MANGKIR','TERLAMBAT') AND c.id_tahun_akademik=?2 GROUP BY a.id_mahasiswa)e ON a.id_mahasiswa=e.id_mahasiswa INNER JOIN jadwal AS f ON a.id_jadwal = f.id INNER JOIN matakuliah_kurikulum AS g ON f.id_matakuliah_kurikulum=g.id WHERE a.id_jadwal=?1 AND a.status='AKTIF'", nativeQuery = true)
    List<ScoreDto>  scoreInput(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "select a.id as krs, b.id as mahasiswa, b.nama as nama, nim,absensi as absmahasiswa, absen_dosen as absdosen,round((absensi * 100) / absen_dosen,2) as absen, round((((absensi * 100) / absen_dosen)*bobot_presensi)/100,2) as nilaiabsen,0 as sds,nilai_uts as uts, \n" +
            "nilai_uas as uas, nilai_akhir as akhir, grade from krs_detail as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and id_jadwal = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            "inner join jadwal as g on a.id_jadwal = g.id\n" +
            "left join \n" +
            "(select count(id) as absen_dosen,id_jadwal from presensi_dosen where id_jadwal = ?1 and status = 'AKTIF' and status_presensi = 'HADIR') as c\n" +
            "on a.id_jadwal = c.id_jadwal\n" +
            "left join\n" +
            "(select count(a.id) as absensi,id_mahasiswa,c.id_jadwal from presensi_mahasiswa as a\n" +
            "inner join sesi_kuliah as b on a.id_sesi_kuliah = b.id and a.status_presensi NOT IN ('MANGKIR','TERLAMBAT') \n" +
            "and b.id_jadwal = ?1 and a.status = 'AKTIF'\n" +
            "inner join presensi_dosen as c on b.id_presensi_dosen = c.id and c.status = 'AKTIF' and c.status_presensi = 'HADIR'\n" +
            "group by id_mahasiswa)as d\n" +
            "on a.id_jadwal = d.id_jadwal and b.id = d.id_mahasiswa", nativeQuery = true)
    List<ScoreDto> inputScore(String idJadwal);

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

    @Query(value = "select j.id as id, c.nama_matakuliah as namaMatakuliah, c.nama_matakuliah_english as namaMatakuliahEnglish, c.kode_matakuliah as kodeMatakuliah, f.nama_karyawan as dosen from jadwal as j\n" +
            "inner join matakuliah_kurikulum as b on j.id_matakuliah_kurikulum = b.id\n" +
            "inner join matakuliah as c on b.id_matakuliah= c.id\n" +
            "inner join prodi as d on j.id_prodi = d.id\n" +
            "inner join dosen as e on j.id_dosen_pengampu = e.id\n" +
            "inner join karyawan as f on e.id_karyawan = f.id \n" +
            "where id_tahun_akademik = ?1 and j.status = 'AKTIF' and id_hari is not null and jam_mulai is not null and b.jumlah_sks > 0\n" +
            "order by nama_matakuliah", nativeQuery = true)
    List<MatkulKonversiDto> cariMatkulKonversi(TahunAkademik tahunAkademik);

    List<Jadwal> findByTahunAkademikAndProdiAndHariNotNullAndStatus(TahunAkademik tahunAkademik, Prodi prodi, StatusRecord statusRecord);

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

    @Query(value = "select j.* from jadwal as j where id_tahun_akademik =?1 and " +
            "id_kelas = ?2 and id_matakuliah_kurikulum = ?3 and status = 'AKTIF' and id_hari is null", nativeQuery = true)
    Jadwal cariDosenSebelumnya(TahunAkademik tahunAkademik,Kelas kelas, MatakuliahKurikulum matakuliahKurikulum);

    @Query(value = "select j.id as jadwal, ke.id as idKelas,ke.nama_kelas as kelas, mk.id as mkkur, m.nama_matakuliah as matkul, m.nama_matakuliah_english as course,mk.jumlah_sks as sks,d.id as idDos, k.nama_karyawan as dosen,CONCAT(ke.nama_kelas,'-',m.kode_matakuliah) as validasi from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id\n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id\n" +
            "inner join matakuliah as m on mk.id_matakuliah = m.id inner join kelas as ke on j.id_kelas = ke.id\n" +
            "where j.id = ?1 ", nativeQuery = true)
    ListPlotingDto detailPloting(String id);

    @Query(value = "SELECT * FROM jadwal where id_tahun_akademik = ?1 and id_hari is not null and status = 'AKTIF' and (sesi = '' or sesi is null)", nativeQuery = true)
    List<Jadwal> cariSesiNull(TahunAkademik tahunAkademik);

    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null and final_status not in ('FINAL')", nativeQuery = true)
    List<ListJadwalDto> listJadwalDto( String idProdi, String idTahunAkademik);

    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 and id_number_elearning = ?3 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null and final_status not in ('FINAL')", nativeQuery = true)
    List<ListJadwalDto> byJadwal1( String idProdi, String idTahunAkademik, String idNumber);

    @Query(value = "select id, id_number_elearning as idNumberElearning from jadwal where id_prodi = ?1 and id_tahun_akademik = ?2 and id_number_elearning = ?3 \n" +
            " and status = 'AKTIF' and id_number_elearning is not null and final_status not in ('FINAL') ", nativeQuery = true)
    List<ListJadwalDto> byJadwal2Dosen(String idProdi,String idTahunAkademik, String idNumber);

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

    @Query(value = "select a.id from jadwal as a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "where id_tahun_akademik = ?1 and b.sds is not null and a.id_prodi = ?2 and a.id = ?1 and a.status = 'AKTIF' and b.sds > 0\n" +
            "and id_hari is not null", nativeQuery = true)
    List<String> findSds3(String idTahunAkademik, String idProdi, String idJadwal);

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

    @Query(value = "select a.id_jadwal as jadwal,a.id_dosen as dosen,a.nama_karyawan as nama,a.email as email,a.nama_matakuliah as matkul,a.nama_kelas as kelas,a.sks as sks,a.month as bulan,a.day as hari,a.date as tanggal,a.total_sesi as totalsesi,a.total_sks as totalsks,coalesce(total_seluruh_sesi,0) as totalseluruhsesi, sks * coalesce(total_seluruh_sesi,0) as totalseluruhsks,idprodi,prodi,idjenjang,jenjang from\n" +
            "(select id_jadwal,id_dosen, nama_karyawan, email, nama_matakuliah, nama_kelas, jumlah_sks as sks, month, day, group_concat(date order by date) as date, sum(sesi) as total_sesi,\n" +
            "jumlah_sks * sum(sesi) as total_sks,idprodi,prodi,idjenjang,jenjang from\n" +
            "(select a.id_jadwal,a.id_dosen, h.nama_karyawan, email, d.nama_matakuliah, e.nama_kelas, c.jumlah_sks,monthname(waktu_masuk) as month, dayname(waktu_masuk) as day, \n" +
            "day(waktu_masuk)as date, jam_mulai, waktu_masuk,1 as sesi,p.id as idProdi, p.nama_prodi as prodi,j.id as idjenjang,j.nama_jenjang as jenjang from presensi_dosen as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "inner join kelas as e on b.id_kelas = e.id \n" +
            "inner join hari as f on b.id_hari = f.id\n" +
            "inner join prodi as p on b.id_prodi = p.id\n" +
            "inner join jenjang as j on p.id_jenjang = j.id\n" +
            "inner join dosen as g on a.id_dosen = g.id\n" +
            "inner join karyawan as h on g.id_karyawan = h.id \n" +
            "where year(a.waktu_masuk) = ?1 and month(a.waktu_masuk)=?2 and a.status = 'AKTIF' and status_presensi in ('HADIR','TERLAMBAT') \n" +
            "group by nama_karyawan, nama_matakuliah, nama_kelas, waktu_masuk\n" +
            "order by nama_karyawan, nama_kelas) as a\n" +
            "group by nama_karyawan, nama_matakuliah, nama_kelas) as a\n" +
            "left join\n" +
            "(select id_jadwal,id_dosen,d.jumlah_sks,count(id_jadwal) as total_seluruh_sesi from presensi_dosen as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join matakuliah_kurikulum as d on b.id_matakuliah_kurikulum = d.id\n" +
            "where tanggal_mulai <= date(concat(?1,'-',?2,'-','01')) and c.tanggal_selesai >= date(concat(?1,'-',?2,'-','01')) \n" +
            "and a.status = 'AKTIF' and a.status_presensi in ('HADIR','TERLAMBAT') \n" +
            "group by id_jadwal, id_dosen) as b on a.id_jadwal = b.id_jadwal and a.id_dosen = b.id_dosen", nativeQuery = true)
    List<RekapAbsenDosen> importAbsen (String tahun, String bulan);

    @Query(value = "select a.id,d.nama_prodi, e.nama_kelas, c.kode_matakuliah, c.nama_matakuliah, c.nama_matakuliah_english, h.nama_karyawan as dosen, a.id_number_elearning, id_tahun_akademik\n" +
            "from jadwal as a\n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id\n" +
            "inner join prodi as d on a.id_prodi = d.id\n" +
            "inner join kelas as e on a.id_kelas = e.id\n" +
            "inner join jadwal_dosen as f on a.id = f.id_jadwal\n" +
            "inner join dosen as g on f.id_dosen = g.id\n" +
            "inner join karyawan as h on g.id_karyawan = h.id\n" +
            "where a.id_tahun_akademik = ?1 and a.id_hari is not null and a.status = 'AKTIF' \n" +
            "and a.id_number_elearning is null or a.id_number_elearning = '' and f.status_jadwal_dosen = 'PENGAMPU' order by e.nama_kelas;" , nativeQuery = true)
    List<Jadwal> listJadwalEnroll (String tahun);

    @Query(value = "select ke.nama_kelas,m.kode_matakuliah,m.nama_matakuliah,\n" +
            "mk.jumlah_sks,k.nama_karyawan,j.status_uts,j.id,j.status_uas  from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id\n" +
            "inner join matakuliah as m on mk.id_matakuliah = m.id inner join dosen as d on j.id_dosen_pengampu = d.id \n" +
            "inner join kelas as ke on j.id_kelas = ke.id\n" +
            "inner join karyawan as k on  d.id_karyawan = k.id\n" +
            " where j.status = 'AKTIF' and j.id_tahun_akademik = ?1 and j.id_hari is not null and j.jam_mulai is not null order by ke.nama_kelas", nativeQuery = true)
    List<Object[]> cariJadwalUjian(TahunAkademik tahunAkademik);

    Jadwal findByProdiAndIdNumberElearningAndStatus(Prodi prodi,String idNumber,StatusRecord statusRecord);

    List<Jadwal> findByTahunAkademikAndStatusAndMatakuliahKurikulumIn(TahunAkademik tahunAkademik, StatusRecord status, List<MatakuliahKurikulum> idMatkur);

    @Query(value = "select j.id as jadwal,h.nama_hari as hari, k.id as idKelas,k.nama_kelas as kelas,mk.id as mkkur,m.kode_matakuliah as kode, m.nama_matakuliah as matkul," +
            "m.nama_matakuliah_english as course, mk.jumlah_sks as sks,d.id as idDos, ka.nama_karyawan as dosen from jadwal as j " +
            "inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id left join hari as h on j.id_hari = h.id inner join matakuliah as m on mk.id_matakuliah = m.id " +
            "inner join kelas as k on j.id_kelas = k.id inner join dosen as d on j.id_dosen_pengampu = d.id" +
            " inner join karyawan as ka on d.id_karyawan = ka.id where j.id_tahun_akademik = ?1 and j.id_hari is null and j.status = 'AKTIF' and j.jam_mulai is null order by k.nama_kelas asc LIMIT ?2 OFFSET ?3", nativeQuery = true)
    List<DataPlotingDto> detailJadwalKosongLimit(TahunAkademik tahunAkademik, Integer limit, Integer offser);

    @Query(value = "select j.id as jadwal,h.nama_hari as hari, k.id as idKelas,k.nama_kelas as kelas,mk.id as mkkur,m.kode_matakuliah as kode, m.nama_matakuliah as matkul," +
            "m.nama_matakuliah_english as course, mk.jumlah_sks as sks,d.id as idDos, ka.nama_karyawan as dosen from jadwal as j " +
            "inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id left join hari as h on j.id_hari = h.id inner join matakuliah as m on mk.id_matakuliah = m.id " +
            "inner join kelas as k on j.id_kelas = k.id inner join dosen as d on j.id_dosen_pengampu = d.id" +
            " inner join karyawan as ka on d.id_karyawan = ka.id where j.id_tahun_akademik = ?1 and j.status = 'AKTIF'  and j.id_hari is null and j.jam_mulai is null order by k.nama_kelas asc", nativeQuery = true)
    List<DataPlotingDto> detailJadwalKosong(TahunAkademik tahunAkademik);

    @Query(value = "select j.id as jadwal,h.nama_hari as hari, k.id as idKelas,k.nama_kelas as kelas,mk.id as mkkur,m.kode_matakuliah as kode, m.nama_matakuliah as matkul," +
            "m.nama_matakuliah_english as course, mk.jumlah_sks as sks,d.id as idDos, ka.nama_karyawan as dosen from jadwal as j " +
            "inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id left join hari as h on j.id_hari = h.id inner join matakuliah as m on mk.id_matakuliah = m.id " +
            "inner join kelas as k on j.id_kelas = k.id inner join dosen as d on j.id_dosen_pengampu = d.id" +
            " inner join karyawan as ka on d.id_karyawan = ka.id where j.id_tahun_akademik = ?1 and j.status = 'AKTIF' and (m.nama_matakuliah like %?2% or k.nama_kelas like %?2% or ka.nama_karyawan like %?2% ) and j.id_hari is null and j.jam_mulai is null order by k.nama_kelas asc LIMIT ?3 OFFSET ?4", nativeQuery = true)
    List<DataPlotingDto> detailJadwalKosongSearchLimit(TahunAkademik tahunAkademik, String search , Integer limit, Integer offset);

    @Query(value = "select j.id as jadwal,h.nama_hari as hari, k.id as idKelas,k.nama_kelas as kelas,mk.id as mkkur,m.kode_matakuliah as kode, m.nama_matakuliah as matkul," +
            "m.nama_matakuliah_english as course, mk.jumlah_sks as sks,d.id as idDos, ka.nama_karyawan as dosen from jadwal as j " +
            "inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id left join hari as h on j.id_hari = h.id inner join matakuliah as m on mk.id_matakuliah = m.id " +
            "inner join kelas as k on j.id_kelas = k.id inner join dosen as d on j.id_dosen_pengampu = d.id" +
            " inner join karyawan as ka on d.id_karyawan = ka.id where j.id_tahun_akademik = ?1 and j.status = 'AKTIF' and (m.nama_matakuliah like %?2% or k.nama_kelas like %?2% or ka.nama_karyawan like %?2% ) and j.id_hari is null and j.jam_mulai is null order by k.nama_kelas asc ", nativeQuery = true)
    List<DataPlotingDto> detailJadwalKosongSearch(TahunAkademik tahunAkademik, String search );

    @Query(value = "select s.sesi as id,s.id as idSesi,s.nama_sesi as namaSesi,TIME_FORMAT(s.jam_mulai, \"%H:%i\") as mulai,TIME_FORMAT(s.jam_selesai, \"%H:%i\") as selesai,aa.* from sesi as s left join\n" +
            "(select 'Bentrok Dosen' as bentrok, k.nama_karyawan as dosen, m.nama_matakuliah as matkul,j.sesi from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id\n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id \n" +
            "where j.id_tahun_akademik = ?1 and j.status = 'AKTIF' and j.id_hari = ?2 \n" +
            "and j.id_dosen_pengampu = ?7 and j.id not in (?3)\n" +
            "union\n" +
            "select 'Bentrok Ruangan' as bentrok, k.nama_karyawan as dosen, m.nama_matakuliah as matkul,j.sesi from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id\n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id \n" +
            "where j.id_tahun_akademik = ?1 and j.id_ruangan = ?4 and j.status = 'AKTIF' and j.id_hari = ?2 and j.id not in (?3) \n" +
            "union\n" +
            "select 'Bentrok Kelas' as bentrok, k.nama_karyawan as dosen, m.nama_matakuliah as matkul,j.sesi from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id\n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id \n" +
            "where j.id_tahun_akademik = ?1 and j.id_kelas = ?5 and j.status = 'AKTIF' and j.id_hari = ?2 and j.id not in (?3)) aa on s.sesi = aa.sesi where sks = ?6 group by if(bentrok is not null,trim(s.sesi),id)", nativeQuery = true)
    List<ValidasiSesi> validateSesi(TahunAkademik tahunAkademik, Hari hari, String id, Ruangan ruangan, Kelas kelas, Integer sks, Dosen dosen);

    List<Jadwal> findByStatusAndTahunAkademikAndFinalPloting(StatusRecord status, TahunAkademik tahun, String ploting);

    @Query(value = "SELECT j.* from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id inner join kelas as k on j.id_kelas = k.id inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan ka on d.id_karyawan = ka.id inner join hari as h on j.id_hari = h.id\n" +
            "where k.nama_kelas like '%-23%' and j.status = 'AKTIF' and id_hari is not null and id_tahun_akademik = 'f635cb68-3da5-4e11-8213-7020636034a2' and j.id not in ('002164c2-d804-4355-a014-b7608996ec3d','0f799d14-6c73-4113-bdaf-204be47db480','133111a3-7d2a-4fc7-a35f-b7dda37d0255','1384495c-15eb-4ba8-8d4a-d4c12c499072','14012981-36ec-4169-ab0b-c5ac22437c2a','1cdb39a3-4653-4a7d-bd58-94a57b5ff85a','23a92eed-9bd7-4c74-ab1c-488daf248e66','28c6de03-ce42-4182-9224-16bd179898c0','2e5298b0-9b0e-4290-9b86-e6b8b4a0ab27','2f4d1017-813a-4a76-b3ea-3afd570e1262','374229bd-6511-442c-9f79-6493ee715c36','39300f98-2ce2-46cd-b167-79461bd83131','3a25273f-6393-4ad1-b309-5d3fbe717459','3bfa2e82-a365-48d3-a8ad-12d073ed0290','3defd21f-2c13-4d9a-a31f-eaebd7b795bb','40a86408-c688-425c-a518-6dbb2140dd02','428d5298-50b5-4032-a68a-c2b0e1e0a6be','48b9cf5f-faac-4d79-b765-4eef399321cc','4dd73a00-145b-4785-a78f-49b275167ffd','511377d1-f2d7-49e8-a8ab-79b9493e7fd1','518417ac-0a0a-4a0f-9d6b-3864b9dffad7','5cfc3e0f-3dff-47b7-8e51-a3145c14dcc5','62fb955e-c3c8-4760-a6a2-61bdf9681c65','6bc9501c-2936-4a4a-9501-d880f1eb76df','8312bef2-3429-45b5-b135-34f051659647','837d30fc-6cb7-412f-95a2-2193cf056646','868ca1c5-a2d7-4832-a6a9-d70cd84aac55','8acbbf4d-516a-4cdf-b1a0-a70da07bebbf','8bfd7efe-cf6d-4f71-821f-6dfa33cf23f9','969969a7-3900-45e9-85ef-1870684d1cc9','9880ea61-fe9e-4ad0-9c8b-15536e2b58b0','a55fb4b9-bae8-41c2-bf6e-68909b79fad5','b7fafe03-bbce-4e23-ade7-5e0af5bf67f7','bb51cfa3-e965-43c8-bde7-fb8d4d39eede','c1f8547c-9ec5-4f5d-883a-b3d9ae182bca','c46e242c-d95e-4f06-bea9-efac2c8a5000','c8754681-e3ce-4b93-9828-66742dfe49f3','cc6e6853-7f4a-4a1b-bb56-bcda207a06cc','cdf62104-bc32-44e6-82a7-e1a806f6f54e','cf3083c4-995b-4b55-b809-238e1cd99f87','d220276b-4d54-4576-bfd4-79e0ce95d379','d988a4d8-fc1b-4ac5-80be-6714a0b22738','dd398971-e8fc-4c6e-b30a-c5286feb2089','e449ba05-7a61-4180-b13c-af9cf6a0b683','ec65e466-8fe5-4a7f-9249-8518dd3e267c','ec884c99-91f3-4881-b861-13ab6885668d','faeb7d40-03bc-4ac1-b24e-b96ab5e9302b','ffcaba3b-f040-4f02-ba72-82e9a8c1cef3','1f57e6b1-493f-4e9c-9be9-c2284b5a7312','0f5f99fa-8701-46cc-a8df-697b82743a9d','65ebe583-4dba-45b7-aaf3-765563d2bd65','8b9ae8ea-133c-4526-96a5-70f548764705','b54011bd-8465-4945-add7-c01f171b920f','2e39ab16-8c3b-4cf3-8867-1af1a1f5cf2c','37886f9d-4dc4-401b-bf2c-b0771a86335c','4e212014-dc61-44b7-bb33-a6c82ec63753','77554611-6935-4503-a1c7-7b7b63d41ea7','c933c250-96f7-4402-ab96-2f8ee25807b5'\n" +
            ") order by h.id", nativeQuery = true)
    List<Jadwal> jadwalMatrik();

    @Query(value = "select j.id as jadwal,m.nama_matakuliah as matakuliah, k.nama_karyawan as dosen, h.nama_hari as hari, r.nama_ruangan as ruangan, ke.nama_kelas as kelas  from jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id" +
            " inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id inner join hari as h on j.id_hari = h.id inner join ruangan as r on j.id_ruangan = r.id inner join kelas as ke on j.id_kelas = ke.id \n" +
            "where j.status = 'AKTIF' and j.id_hari is not null and j.id_ruangan is not null and j.jam_mulai is not null and m.nama_matakuliah_english = 'English Language' and j.id_tahun_akademik = ?1",
            nativeQuery = true)
    List<JadwalTlc> jadwalEnglish(TahunAkademik tahunAkademik);

}

