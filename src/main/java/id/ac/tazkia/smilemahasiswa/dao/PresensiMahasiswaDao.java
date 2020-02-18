package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.bkd.Attendance;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {
    List<PresensiMahasiswa> findBySesiKuliahAndStatus(SesiKuliah sesiKuliah, StatusRecord aktif);

    List<PresensiMahasiswa> findBySesiKuliah(SesiKuliah sesiKuliah);

    PresensiMahasiswa findByMahasiswaAndSesiKuliahAndStatus(Mahasiswa m, SesiKuliah sesiKuliah, StatusRecord aktif);

    Long countByStatusPresensiNotInAndKrsDetailAndStatus(List<StatusPresensi> statusp, KrsDetail krsDetail, StatusRecord aktif);

    Long countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord aktif, StatusRecord aktif1, List<StatusPresensi> statusp, Mahasiswa mahasiswa, String sds);

    @Query(value = "SELECT * FROM (SELECT b.id,b.nim,b.nama FROM krs_detail AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id  WHERE a.status='AKTIF' AND a.id_jadwal=?1 ORDER BY nim)a LEFT JOIN (SELECT a.id_mahasiswa,GROUP_CONCAT(LEFT(a.status_presensi,1)) AS presensi FROM presensi_mahasiswa AS a INNER JOIN  sesi_kuliah AS b ON a.id_sesi_kuliah=b.id INNER JOIN  presensi_dosen AS c ON b.id_presensi_dosen=c.id WHERE a.status='AKTIF' AND c.status='AKTIF' AND c.id_jadwal=?1 GROUP BY a.id_mahasiswa ORDER BY id_mahasiswa)b ON a.id=b.id_mahasiswa ORDER BY nim", nativeQuery = true)
    List<Attendance> bkdAttendance(Jadwal jadwal);

    @Query(value = "select mahasiswa.nim, mahasiswa.nama, krs_detail.nilai_akhir, krs_detail.grade, krs_detail.bobot from krs_detail inner join mahasiswa on mahasiswa.id = krs_detail.id_mahasiswa where krs_detail.id_jadwal =?1 and krs_detail.status = 'AKTIF' order by mahasiswa.nim", nativeQuery = true)
    List<Object[]> bkdNilai(Jadwal jadwal);
}
