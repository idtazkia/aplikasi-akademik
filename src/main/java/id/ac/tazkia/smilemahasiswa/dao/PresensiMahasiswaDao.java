package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.bkd.Attendance;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {
    List<PresensiMahasiswa> findBySesiKuliahAndStatus(SesiKuliah sesiKuliah, StatusRecord aktif);

    List<PresensiMahasiswa> findBySesiKuliah(SesiKuliah sesiKuliah);

    PresensiMahasiswa findByMahasiswaAndSesiKuliahAndStatus(Mahasiswa m, SesiKuliah sesiKuliah, StatusRecord aktif);

    Long countByStatusPresensiNotInAndKrsDetailAndStatus(List<StatusPresensi> statusp, KrsDetail krsDetail, StatusRecord aktif);

    Long countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord aktif, StatusRecord aktif1, List<StatusPresensi> statusp, Mahasiswa mahasiswa, String sds);

    @Query(value = "CALL getabsensi(?1)", nativeQuery = true)
    List<Object[]> bkdAttendance(Jadwal jadwal);

    @Query(value = "select mahasiswa.nim, mahasiswa.nama, krs_detail.nilai_akhir, krs_detail.grade, krs_detail.bobot from krs_detail inner join mahasiswa on mahasiswa.id = krs_detail.id_mahasiswa where krs_detail.id_jadwal =?1 and krs_detail.status = 'AKTIF' order by mahasiswa.nim", nativeQuery = true)
    List<Object[]> bkdNilai(Jadwal jadwal);
}
