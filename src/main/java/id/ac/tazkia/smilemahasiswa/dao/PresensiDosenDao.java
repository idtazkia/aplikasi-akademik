package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen,String> {
    Long countByStatusAndJadwal(StatusRecord aktif, Jadwal jadwal);

    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(Jadwal j, Dosen d, TahunAkademik tahunAkademik, Hari hari, StatusRecord aktif);
    
    @Query(value = "SELECT DATE(a.waktu_masuk)AS tanggal,TIME(a.waktu_masuk)AS jam_masuk,TIME(a.waktu_selesai)AS jam_selesai,berita_acara,mhsw,COALESCE(miss,0) AS miss, d.nama_karyawan AS dosen FROM presensi_dosen AS a INNER JOIN sesi_kuliah AS b ON a.id=b.id_presensi_dosen INNER JOIN dosen AS c ON a.id_dosen=c.id INNER JOIN karyawan AS d ON c.id_karyawan=d.id LEFT JOIN (SELECT COUNT(a.id)AS mhsw,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND b.status='AKTIF' GROUP BY id_sesi_kuliah)e ON b.id=e.id_sesi_kuliah LEFT JOIN (SELECT COUNT(a.id)AS miss,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND a.status_presensi IN('MANGKIR','TERLAMBAT','IZIN') AND b.status='AKTIF' GROUP BY id_sesi_kuliah)f ON b.id=f.id_sesi_kuliah WHERE a.id_jadwal =?1 AND a.status='AKTIF' ORDER BY DATE(a.waktu_masuk)", nativeQuery = true)
    List<Object[]> bkdBeritaAcara (Jadwal jadwal);
}
