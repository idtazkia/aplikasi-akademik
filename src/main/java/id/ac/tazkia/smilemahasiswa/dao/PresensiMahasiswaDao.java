package id.ac.tazkia.smilemahasiswa.dao;

import java.time.LocalDateTime;
import java.util.List;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.dto.NilaiAbsenSdsDto;
import id.ac.tazkia.smilemahasiswa.dto.api.PerformanceMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.attendance.DetailPresensiDto;
import id.ac.tazkia.smilemahasiswa.dto.attendance.PresensiMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.mahasiswa.MahasiswaMangkirTigaDto;

import javax.transaction.Transactional;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {
        List<PresensiMahasiswa> findBySesiKuliahAndStatus(SesiKuliah sesiKuliah, StatusRecord aktif);

        List<PresensiMahasiswa> findBySesiKuliah(SesiKuliah sesiKuliah);

        PresensiMahasiswa findByMahasiswaAndSesiKuliahAndStatus(Mahasiswa m, SesiKuliah sesiKuliah, StatusRecord aktif);

        @Query(value = "select count(a.id)as jml_mangkir from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah = b.id inner join presensi_dosen as c on b.id_presensi_dosen = c.id where a.id_mahasiswa=?1 and a.id_krs_detail=?2 and a.status='AKTIF' and c.status='AKTIF' and a.status_presensi in ('MANGKIR','TERLAMBAT')", nativeQuery = true)
        Long jumlahMangkir(Mahasiswa mahasiswa, KrsDetail krsDetail);

        @Query(value = "CALL getabsensi(?1)", nativeQuery = true)
        List<Object[]> bkdAttendance(Jadwal jadwal);

        @Query(value = "select mahasiswa.nim, mahasiswa.nama, coalesce(krs_detail.nilai_akhir, 0) as nilai, coalesce(krs_detail.grade, '-') as grade, coalesce( krs_detail.bobot, '0.00') as bobot from krs_detail inner join mahasiswa on mahasiswa.id = krs_detail.id_mahasiswa where krs_detail.id_jadwal =?1 and krs_detail.status = 'AKTIF' order by mahasiswa.nim", nativeQuery = true)
        List<Object[]> bkdNilai(Jadwal jadwal);

        @Query(value = "SELECT COUNT(a.id) AS presensiMahasiswa FROM presensi_mahasiswa AS a \n" +
                        "INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah = b.id \n" +
                        "INNER JOIN presensi_dosen AS c ON b.id_presensi_dosen = c.id WHERE c.status='AKTIF' AND a.status ='AKTIF' AND a.id_krs_detail = ?1", nativeQuery = true)
        Integer jumlahPresensi(String idKrsDetail);

        @Query(value = "select a.id_mahasiswa as idMahasiswa,a.presensi_dosen as presensiDosen, coalesce(count(d.id),0) as presensiMahasiswa, (10/a.presensi_dosen)*coalesce(count(d.id),0) as nilai from\n"
                        +
                        "(select a.id_mahasiswa, a.id as id_krs, b.id as id_jadwal, e.kode_tahun_akademik, count(f.id) as presensi_dosen from krs_detail as a \n"
                        +
                        "inner join jadwal as b on a.id_jadwal = b.id\n" +
                        "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
                        "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
                        "inner join tahun_akademik as e on b.id_tahun_akademik = e.id\n" +
                        "inner join presensi_dosen as f on b.id = f.id_jadwal\n" +
                        "where d.kode_matakuliah like '%SDS%' and a.status = 'AKTIF' and a.id_mahasiswa = ?1\n" +
                        "and e.kode_tahun_akademik <= ?2 and b.status = 'AKTIF' \n" +
                        "group by b.id\n" +
                        "order by e.kode_tahun_akademik desc limit 1) as a\n" +
                        "left join presensi_dosen as b on a.id_jadwal = b.id_jadwal\n" +
                        "left join sesi_kuliah as c on b.id = c.id_presensi_dosen\n" +
                        "left join presensi_mahasiswa as d on c.id = d.id_sesi_kuliah and a.id_krs = d.id_krs_detail\n"
                        +
                        "where coalesce(d.status,'HAPUS') = 'AKTIF'", nativeQuery = true)
        NilaiAbsenSdsDto listNilaiAbsenSds(String idMahasiswa, String kodeTahunAkademik);

        @Query(value = "select p.id as idPresensi,p.id_sesi_kuliah as idSesi, m.id as idMahasiswa,p.id_krs_detail as krs, m.nama as nama , m.nim as nim,\n"
                        +
                        " p.status_presensi as presensi, coalesce (cast(p.waktu_masuk as time(0)), '-') as time\n" +
                        "from presensi_mahasiswa as p inner join mahasiswa as m on p.id_mahasiswa = m.id\n" +
                        "where p.status = 'AKTIF' and p.id_sesi_kuliah = ?1", nativeQuery = true)
        List<PresensiMahasiswaDto> listPresensiMahasiswa(SesiKuliah sesiKuliah);

        @Query(value = "select p.id as idPresensi,p.id_sesi_kuliah as idSesi, m.id as idMahasiswa,p.id_krs_detail as krs, m.nama as nama , m.nim as nim,\n"
                        +
                        " p.status_presensi as presensi, coalesce (cast(p.waktu_masuk as time(0)), '-') as time\n" +
                        "from presensi_mahasiswa as p inner join mahasiswa as m on p.id_mahasiswa = m.id\n" +
                        "where p.status = 'AKTIF' and p.id_sesi_kuliah = ?1 and p.catatan = 'Barcode'", nativeQuery = true)
        List<PresensiMahasiswaDto> listPresensiMahasiswaBarcode(SesiKuliah sesiKuliah);

        @Query(value = "SELECT m.id AS idMahasiswa,m.nama AS nama,m.nim AS nim,kd.id AS idKrs,COALESCE(pm.id, '-') AS idPresensi,COALESCE(sk.id, '-') AS idSesi, COALESCE(pm.status_presensi, 'UNKNOWN') AS presensi, COALESCE(CAST(pm.waktu_masuk AS TIME), '-') AS time" +
                " FROM krs_detail AS kd INNER JOIN sesi_kuliah AS sk ON kd.id_jadwal = sk.id_jadwal INNER JOIN mahasiswa AS m ON kd.id_mahasiswa = m.id LEFT JOIN presensi_mahasiswa AS pm ON pm.id_sesi_kuliah = sk.id AND pm.id_mahasiswa = kd.id_mahasiswa AND pm.status = 'AKTIF' " +
                "WHERE kd.status = 'AKTIF' AND sk.id = ?1 AND kd.status_konversi IS NULL", nativeQuery = true)
        List<PresensiMahasiswaDto> listDataPresensiMahasiswa(SesiKuliah sesiKuliah);

//    @Query(value = "SELECT COUNT(*) as jumlahMahasiswaOntime, COUNT(*) * 100 / (SELECT COUNT(*) as semuaMahasiswa\n" +
//            "\tFROM presensi_mahasiswa as pm\n" +
//            "    inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
//            "\tinner join jadwal as jd on sk.id_jadwal = jd.id\n" +
//            "\twhere jd.id_tahun_akademik = ?1 and pm.status = 'AKTIF')\n" +
//            "as persentase\n" +
//            "FROM presensi_mahasiswa as pm\n" +
//            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
//            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
//            "where jd.id_tahun_akademik = ?1 and pm.status = 'AKTIF' and pm.status_presensi = ?2", nativeQuery = true)
//    Object jmlMahasiswa(String tahunAkademik, String statusPresensi);

//    @Query(value = "SELECT \n" +
//            "  COUNT(CASE WHEN pm.status_presensi = ?2 THEN 1 END) AS jumlahMahasiswaOntime, \n" +
//            "  COUNT(CASE WHEN pm.status_presensi = ?2 THEN 1 END) * 100 / (\n" +
//            "    SELECT COUNT(*) \n" +
//            "    FROM presensi_mahasiswa as pm2\n" +
//            "    inner join sesi_kuliah as sk2 on pm2.id_sesi_kuliah = sk2.id\n" +
//            "    inner join jadwal as jd2 on sk2.id_jadwal = jd2.id\n" +
//            "    where jd2.id_tahun_akademik = ?1 \n" +
//            "      and pm2.status = 'AKTIF'\n" +
//            "      and EXISTS (\n" +
//            "        SELECT *\n" +
//            "        FROM sesi_kuliah as sk3\n" +
//            "        inner join jadwal as jd3 on sk3.id_jadwal = jd3.id\n" +
//            "        where sk3.id = pm2.id_sesi_kuliah\n" +
//            "          and jd3.id_tahun_akademik = ?1\n" +
//            "      )\n" +
//            "  ) as persentase\n" +
//            "FROM presensi_mahasiswa as pm\n" +
//            "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
//            "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
//            "where jd.id_tahun_akademik = ?1 \n" +
//            "  and pm.status = 'AKTIF' \n" +
//            "  and pm.status_presensi = ?2\n", nativeQuery = true)
//    Object jmlMahasiswa(String tahunAkademik, String statusPresensi);

//        @Query(value = "SELECT \n" +
//                "  COUNT(pm.status_presensi = ?2) AS jumlahMahasiswaOntime,\n" +
//                "  COUNT(pm.status_presensi = ?2) * 100 / totalMahasiswa.semuaMahasiswa AS persentase\n" +
//                "FROM presensi_mahasiswa as pm\n" +
//                "INNER JOIN (\n" +
//                "  SELECT COUNT(*) as semuaMahasiswa\n" +
//                "  FROM presensi_mahasiswa as pm2\n" +
//                "  INNER JOIN sesi_kuliah as sk2 ON pm2.id_sesi_kuliah = sk2.id\n" +
//                "  INNER JOIN jadwal as jd2 ON sk2.id_jadwal = jd2.id\n" +
//                "  WHERE jd2.id_tahun_akademik = ?1 AND pm2.status = 'AKTIF'\n" +
//                ") AS totalMahasiswa\n" +
//                "INNER JOIN sesi_kuliah as sk ON pm.id_sesi_kuliah = sk.id\n" +
//                "INNER JOIN jadwal as jd ON sk.id_jadwal = jd.id\n" +
//                "WHERE jd.id_tahun_akademik = ?1 AND pm.status = 'AKTIF' AND pm.status_presensi = ?2", nativeQuery = true)
//        Object jmlMahasiswa(String tahunAkademik, String statusPresensi);
//
//        @Query(value = "SELECT pm.id, jd.id_tahun_akademik, pm.id_krs_detail,pr.nama_prodi,pm.id_mahasiswa,ms.nama, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as id_jadwal, jd.jam_mulai, jd.jam_selesai, count(pm.id) as kehadiran\n" +
//                "FROM presensi_mahasiswa as pm\n" +
//                "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
//                "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
//                "inner join mahasiswa as ms on pm.id_mahasiswa = ms.id\n" +
//                "inner join prodi as pr on jd.id_prodi = pr.id\n" +
//                "where jd.id_tahun_akademik = ?1 and pm.status = 'AKTIF' and pm.status_presensi = ?2\n" +
//                "group by id_mahasiswa\n" +
//                "order by count(pm.id) DESC",countQuery = "SELECT COUNT(id) as id from\n" +
//                "(SELECT pm.id, jd.id_tahun_akademik, pm.id_krs_detail,pr.nama_prodi,pm.id_mahasiswa,ms.nama, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as id_jadwal, jd.jam_mulai, jd.jam_selesai, count(pm.id) as kehadiran\n" +
//                "FROM presensi_mahasiswa as pm\n" +
//                "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
//                "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
//                "inner join mahasiswa as ms on pm.id_mahasiswa = ms.id\n" +
//                "inner join prodi as pr on jd.id_prodi = pr.id\n" +
//                "where jd.id_tahun_akademik = ?1 and pm.status = 'AKTIF' and pm.status_presensi = ?2\n" +
//                "group by id_mahasiswa\n" +
//                "order by count(pm.id) DESC) as a",nativeQuery = true)
//        Page<Object> klasemenMahasiswa(String tahunAkademik, String statusPresensi, Pageable pagebable);

        @Query(value = "select bulan, round((coalesce(ontime)*100)/(coalesce(ontime,0)+coalesce(late,0)),0) as rate from\n"
                        +
                        "(select monthname(waktu_masuk)as bulan, d.id,sum(if(a.status_presensi = 'HADIR', 1,0)) as ontime, sum(if(a.status_presensi <> 'HADIR',1,0)) as late from presensi_mahasiswa as a\n"
                        +
                        "inner join krs_detail as b on a.id_krs_detail = b.id\n" +
                        "inner join jadwal as c on b.id_jadwal = c.id\n" +
                        "inner join tahun_akademik as d on c.id_tahun_akademik = d.id\n" +
                        "where a.status = 'AKTIF' and b.status = 'AKTIF' and c.status = 'AKTIF' and d.status = 'AKTIF' \n"
                        +
                        "and year(a.waktu_masuk) = year(now()) and month(a.waktu_masuk) = month(now())\n" +
                        "and d.tanggal_mulai <= date(now()) and d.tanggal_selesai_nilai >= date(now()))as a\n", nativeQuery = true)
        PerformanceMahasiswaDto performanceMahasiswa();

        @Query(value = "select aa.status_presensi as presensi, coalesce(aa.waktu_masuk,bb.waktu_mulai) as masuk, coalesce(aa.waktu_keluar,bb.waktu_selesai)as keluar, bb.berita_acara as berita from (select a.* from presensi_mahasiswa as a inner join krs_detail as b on a.id_krs_detail=b.id inner join krs as c on b.id_krs=c.id where a.id_mahasiswa=?1 and c.id_tahun_akademik=?2 and a.status='AKTIF' and b.status='AKTIF' and c.status='AKTIF' and b.id_jadwal=?3)aa inner join sesi_kuliah as bb on aa.id_sesi_kuliah=bb.id inner join (select * from presensi_dosen where status='AKTIF' and id_jadwal=?3) cc on bb.id_presensi_dosen=cc.id order by bb.waktu_mulai", nativeQuery = true)
        List<DetailPresensiDto> detailPresensi(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Jadwal jadwal);

        @Query(value = "select kd.id, pm.id as id_presensi_mahasiswa, jd.id_tahun_akademik, pm.id_krs_detail,pm.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.email_tazkia as email, pm.waktu_masuk, pm.waktu_keluar,pm.status_presensi, pm.status, jd.id as idJadwal,jd.id_number_elearning as idNumberElearning, jd.jam_mulai, jd.jam_selesai\n" +
                "from krs_detail as kd\n" +
                "left join presensi_mahasiswa as pm on kd.id = pm.id_krs_detail\n" +
                "inner join sesi_kuliah as sk on pm.id_sesi_kuliah = sk.id\n" +
                "inner join jadwal as jd on sk.id_jadwal = jd.id\n" +
                "inner join mahasiswa as ma on pm.id_mahasiswa = ma.id\n" +
                "where jd.id_tahun_akademik = ?1 and jd.id_number_elearning = ?2 and pm.status_presensi IN ('MANGKIR','TERLAMBAT') and pm.status = 'AKTIF' and kd.status = 'AKTIF'\n" +
                "group by pm.id_mahasiswa, jd.id\n" +
                "having count(pm.id) > 3\n" +
                "order by count(pm.id) DESC", nativeQuery = true)
        List<MahasiswaMangkirTigaDto> listMahasiswaMangkirTigaDto(String idTahunAkademik, String idJadwal);

        Integer countByStatusPresensiNotInAndStatusAndKrsDetail(List<StatusPresensi> presensi, StatusRecord status, KrsDetail krsDetail);

        @Modifying
        @Transactional
        @Query(value = "INSERT INTO presensi_mahasiswa (id, id_krs_detail, id_sesi_kuliah, id_mahasiswa, waktu_masuk, waktu_keluar, status_presensi, catatan, rating, status, statusimport) " +
                "SELECT uuid(), kd.id, sk.id, m.id, NOW(), sk.waktu_selesai, ?2, 'Manual', 0, 'AKTIF', null " +
                "FROM krs_detail kd " +
                "INNER JOIN sesi_kuliah sk ON kd.id_jadwal = sk.id_jadwal " +
                "INNER JOIN mahasiswa m ON kd.id_mahasiswa = m.id " +
                "LEFT JOIN presensi_mahasiswa pm ON pm.id_sesi_kuliah = sk.id AND pm.id_mahasiswa = kd.id_mahasiswa " +
                "WHERE kd.status = 'AKTIF' AND sk.id = ?1 AND pm.id IS NULL AND kd.status_konversi IS NULL", nativeQuery = true)
        void insertNewPresensiMahasiswa(String sesiKuliahId, String status);

        @Modifying
        @Transactional
        @Query(value = "UPDATE presensi_mahasiswa pm " +
                "SET pm.status_presensi = ?2, pm.catatan = 'Manual', pm.waktu_masuk = NOW(), pm.waktu_keluar = (SELECT sk.waktu_selesai FROM sesi_kuliah sk WHERE sk.id = ?1) " +
                "WHERE pm.id_sesi_kuliah = ?1 AND pm.status = 'AKTIF'", nativeQuery = true)
        void updatePresensiMahasiswa(String sesiKuliahId, String statusPresensi);

}
