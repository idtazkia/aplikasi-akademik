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

    @Query(value = "select count(a.id)as jml_mangkir from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah = b.id inner join presensi_dosen as c on b.id_presensi_dosen = c.id where a.id_mahasiswa=?1 and a.id_krs_detail=?2 and a.status='AKTIF' and c.status='AKTIF' and a.status_presensi in ('MANGKIR','TERLAMBAT')", nativeQuery = true)
    Long jumlahMangkir(Mahasiswa mahasiswa, KrsDetail krsDetail);

    @Query(value = "CALL getabsensi(?1)", nativeQuery = true)
    List<Object[]> bkdAttendance(Jadwal jadwal);

    @Query(value = "select mahasiswa.nim, mahasiswa.nama, krs_detail.nilai_akhir, krs_detail.grade, krs_detail.bobot from krs_detail inner join mahasiswa on mahasiswa.id = krs_detail.id_mahasiswa where krs_detail.id_jadwal =?1 and krs_detail.status = 'AKTIF' order by mahasiswa.nim", nativeQuery = true)
    List<Object[]> bkdNilai(Jadwal jadwal);
}
