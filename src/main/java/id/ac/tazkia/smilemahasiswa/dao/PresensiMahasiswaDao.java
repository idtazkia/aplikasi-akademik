package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.NilaiAbsenSdsDto;
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

    @Query(value = "SELECT COUNT(a.id) AS presensiMahasiswa FROM presensi_mahasiswa AS a \n" +
            "INNER JOIN sesi_kuliah AS b ON a.id_sesi_kuliah = b.id \n" +
            "INNER JOIN presensi_dosen AS c ON b.id_presensi_dosen = c.id WHERE c.status='AKTIF' AND a.status ='AKTIF' AND a.id_krs_detail = ?1", nativeQuery = true)
    Integer jumlahPresensi(String idKrsDetail);

    @Query(value = "select a.id_mahasiswa as idMahasiswa,a.presensi_dosen as presensiDosen, coalesce(count(d.id),0) as presensiMahasiswa, (10/a.presensi_dosen)*coalesce(count(d.id),0) as nilai from\n" +
            "(select a.id_mahasiswa, a.id as id_krs, b.id as id_jadwal, e.kode_tahun_akademik, count(f.id) as presensi_dosen from krs_detail as a \n" +
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
            "left join presensi_mahasiswa as d on c.id = d.id_sesi_kuliah and a.id_krs = d.id_krs_detail\n" +
            "where coalesce(d.status,'HAPUS') = 'AKTIF'", nativeQuery = true)
    NilaiAbsenSdsDto listNilaiAbsenSds(String idMahasiswa, String kodeTahunAkademik);

}
