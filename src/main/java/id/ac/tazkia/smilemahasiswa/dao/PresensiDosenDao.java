package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.attendance.PresensiDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.attendance.RekapPresensiDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;


public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen,String> {
    Long countByStatusAndJadwal(StatusRecord aktif, Jadwal jadwal);

    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(Jadwal j, Dosen d, TahunAkademik tahunAkademik, Hari hari, StatusRecord aktif);

    @Query(value = "SELECT DATE(a.waktu_masuk)AS tanggal,TIME(a.waktu_masuk)AS jam_masuk,TIME(a.waktu_selesai)AS jam_selesai,berita_acara,mhsw,COALESCE(miss,0) AS miss, d.nama_karyawan AS dosen FROM presensi_dosen AS a INNER JOIN sesi_kuliah AS b ON a.id=b.id_presensi_dosen INNER JOIN dosen AS c ON a.id_dosen=c.id INNER JOIN karyawan AS d ON c.id_karyawan=d.id LEFT JOIN (SELECT COUNT(a.id)AS mhsw,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND b.status='AKTIF' GROUP BY id_sesi_kuliah)e ON b.id=e.id_sesi_kuliah LEFT JOIN (SELECT COUNT(a.id)AS miss,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND a.status_presensi IN('MANGKIR','TERLAMBAT','IZIN') AND b.status='AKTIF' GROUP BY id_sesi_kuliah)f ON b.id=f.id_sesi_kuliah WHERE a.id_jadwal =?1 AND a.status='AKTIF' ORDER BY DATE(a.waktu_masuk)", nativeQuery = true)
    List<Object[]> bkdBeritaAcara (Jadwal jadwal);

    @Query(value = "SELECT pd.id as idPresensi, sk.id as sesi,pd.waktu_masuk as masuk, pd.waktu_selesai as selesai,k.nama_karyawan as dosen,sk.berita_acara as berita\n" +
            "FROM presensi_dosen as pd inner join sesi_kuliah as sk on pd.id = sk.id_presensi_dosen inner join dosen as d on pd.id_dosen = d.id inner join karyawan as k on d.id_karyawan = k.id\n" +
            "where pd.status = 'AKTIF' and pd.id_jadwal = ?1 order by pd.waktu_masuk desc", nativeQuery = true)
    List<PresensiDosenDto> listPresensiDosen(Jadwal jadwal);

    @Query(value = "\tselect d.nama_karyawan as dosen, e.nama_kelas as kelas, nama_matakuliah as matkul,b.jam_mulai as mulai, a.waktu_masuk as realisasi,\n" +
            "\tTIMEDIFF(a.waktu_masuk,STR_TO_DATE(concat(date(a.waktu_masuk),' ',b.jam_mulai), '%Y-%m-%d %T')) as rangeWaktu, pertemuan, \n" +
            "\ta.status_presensi as presensi,count(g.id_sesi_kuliah) as jumlahMahasiswa, \n" +
            "\tsum(if(g.status_presensi = 'HADIR',1,0)) as hadir,\n" +
            "\tsum(if(g.status_presensi = 'MANGKIR',1,0)) as mangkir,\n" +
            "\tsum(if(g.status_presensi = 'IZIN',1,0)) as izin,\n" +
            "\tsum(if(g.status_presensi = 'SAKIT',1,0)) as sakit from\n" +
            "\t(select * from presensi_dosen where date(waktu_masuk)= ?1) as a\n" +
            "\tinner join jadwal as b on a.id_jadwal = b.id\n" +
            "\tinner join dosen as c on a.id_dosen = c.id\n" +
            "\tinner join karyawan as d on c.id_karyawan = d.id\n" +
            "\tinner join kelas as e on b.id_kelas = e.id\n" +
            "\tinner join sesi_kuliah as f on a.id = f.id_presensi_dosen\n" +
            "\tinner join matakuliah_kurikulum as h on b.id_matakuliah_kurikulum = h.id\n" +
            "\tinner join matakuliah as i on h.id_matakuliah = i.id\n" +
            "\tinner join presensi_mahasiswa as g on f.id = g.id_sesi_kuliah group by g.id_sesi_kuliah", nativeQuery = true)
    List<RekapPresensiDto> rekapPresensiPerHari(LocalDate localDate);

    PresensiDosen findTopByJadwalAndStatusOrderByWaktuMasukDesc(Jadwal jadwal, StatusRecord status);

    @Query(value = "SELECT COUNT(*) as jumlahKelasOntime, COUNT(*) * 100 / (SELECT COUNT(*) as semuaKelas\n" +
            "\tFROM presensi_dosen as pd\n" +
            "\tinner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "\twhere pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF')\n" +
            "as persentase\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF' and pd.status_presensi = ?2", nativeQuery = true)
    Object jmlKelas(TahunAkademik tahunAkademik, String statusPresensi);

    @Query(value = "SELECT pd.id, pd.id_tahun_akademik, pd.id_jadwal, pd.id_dosen,kr.nama_karyawan,pr.nama_prodi, count(pd.id) as kehadiran\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF' and pd.status_presensi = ?2\n" +
            "group by id_dosen\n" +
            "order by count(pd.id) DESC",countQuery = "SELECT COUNT(id) as id FROM \n" +
            "(SELECT pd.id, pd.id_tahun_akademik, pd.id_jadwal, pd.waktu_masuk, pd.waktu_selesai,pd.status_presensi, pd.status, pd.id_dosen,kr.nama_karyawan,pr.nama_prodi, jd.jam_mulai, jd.jam_selesai, count(pd.id) as kehadiran\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF' and pd.status_presensi = ?2\n" +
            "group by id_dosen\n" +
            "order by count(pd.id) DESC) as a",nativeQuery = true)
    Page<Object[]> klasemenDosen(TahunAkademik tahunAkademik, String statusPresensi, Pageable pageable);

}
