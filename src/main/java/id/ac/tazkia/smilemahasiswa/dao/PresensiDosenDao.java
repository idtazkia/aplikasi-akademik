package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.attendance.PresensiDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.attendance.RekapPresensiDto;
import id.ac.tazkia.smilemahasiswa.dto.elearning.KlasemenDosenIntDto;
import id.ac.tazkia.smilemahasiswa.dto.report.ReportUjianDto;
import id.ac.tazkia.smilemahasiswa.dto.report.SalaryDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;

public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen, String> {
    Long countByStatusAndJadwal(StatusRecord aktif, Jadwal jadwal);

    List<PresensiDosen> findByJadwalAndStatusNotInAndStatusPresensi(Jadwal jadwal, List<StatusRecord> statusRecords, StatusPresensi statusPresensi);

    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(Jadwal j, Dosen d,
                                                                                   TahunAkademik tahunAkademik, Hari hari, StatusRecord aktif);

    @Query(value = "SELECT DATE(a.waktu_masuk)AS tanggal,TIME(a.waktu_masuk)AS jam_masuk,TIME(a.waktu_selesai)AS jam_selesai,berita_acara,mhsw,COALESCE(miss,0) AS miss, d.nama_karyawan AS dosen FROM presensi_dosen AS a INNER JOIN sesi_kuliah AS b ON a.id=b.id_presensi_dosen INNER JOIN dosen AS c ON a.id_dosen=c.id INNER JOIN karyawan AS d ON c.id_karyawan=d.id LEFT JOIN (SELECT COUNT(a.id)AS mhsw,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND b.status='AKTIF' GROUP BY id_sesi_kuliah)e ON b.id=e.id_sesi_kuliah LEFT JOIN (SELECT COUNT(a.id)AS miss,a.id_sesi_kuliah FROM presensi_mahasiswa AS a INNER JOIN krs_detail AS b ON a.id_krs_detail=b.id WHERE b.id_jadwal=?1 AND a.status='AKTIF' AND a.status_presensi IN('MANGKIR','TERLAMBAT','IZIN') AND b.status='AKTIF' GROUP BY id_sesi_kuliah)f ON b.id=f.id_sesi_kuliah WHERE a.id_jadwal =?1 AND a.status='AKTIF' ORDER BY DATE(a.waktu_masuk)", nativeQuery = true)
    List<Object[]> bkdBeritaAcara(Jadwal jadwal);

    @Query(value = "SELECT pd.id as idPresensi, sk.id as sesi,pd.waktu_masuk as masuk,DATE(pd.waktu_masuk) as tanggal, pd.waktu_selesai as selesai,k.nama_karyawan as dosen,sk.berita_acara as berita, sk.pertemuan as pertemuan, sk.pertemuan_ke as pertemuanke, sk.rps as rps\n" + //
                    "FROM presensi_dosen as pd inner join sesi_kuliah as sk on pd.id = sk.id_presensi_dosen inner join dosen as d on pd.id_dosen = d.id inner join karyawan as k on d.id_karyawan = k.id\n" + //
                    "where pd.status in ('AKTIF','NONAKTIF') and pd.id_jadwal = ?1 order by sk.pertemuan_ke asc", nativeQuery = true)
    List<PresensiDosenDto> listPresensiDosen(Jadwal jadwal);

    @Query(value = "SELECT pd.id as idPresensi, sk.id as sesi,pd.waktu_masuk as masuk, pd.waktu_selesai as selesai,k.nama_karyawan as dosen,sk.berita_acara as berita\n"
            +
            "FROM presensi_dosen as pd inner join sesi_kuliah as sk on pd.id = sk.id_presensi_dosen inner join dosen as d on pd.id_dosen = d.id inner join karyawan as k on d.id_karyawan = k.id\n"
            +
            "where pd.status = 'AKTIF' and pd.id_jadwal = ?1 order by sk.pertemuan_ke asc", nativeQuery = true)
    List<PresensiDosenDto> listPresensiDosen2(Jadwal jadwal);


    @Query(value = "\tselect d.nama_karyawan as dosen, e.nama_kelas as kelas, nama_matakuliah as matkul,b.jam_mulai as mulai, a.waktu_masuk + INTERVAL 7 HOUR as realisasi,\n"
            +
            "\tTIMEDIFF(a.waktu_masuk + INTERVAL 7 HOUR,STR_TO_DATE(concat(date(a.waktu_masuk),' ',b.jam_mulai), '%Y-%m-%d %T')) as rangeWaktu, pertemuan, \n"
            +
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

    @Query(value = "SELECT COUNT(*) as jumlahKelasOntime, COUNT(*) * 100 / (SELECT COUNT(*)\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\tINNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "\twhere pd.id_tahun_akademik = ?1 \n" +
            "\tand pd.waktu_masuk <= CURDATE())\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.waktu_masuk <= CURDATE()\n" +
            "and pd.status_presensi = ?2", nativeQuery = true)
    Object jmlKelasOntime(TahunAkademik tahunAkademik, String statusPresensi);

    @Query(value = "SELECT COUNT(*) as jumlahKelasOntime, COUNT(*) * 100 / (SELECT COUNT(*)\n" +
            "\tFROM sesi_kuliah as sk\n" +
            "\tINNER JOIN jadwal as jd on sk.id_jadwal = jd.id\n" +
            "\tINNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "\twhere pd.id_tahun_akademik = ?1 \n" +
            "\tand pd.waktu_masuk <= CURDATE())\n" +
            "as persentase\n" +
            "FROM sesi_kuliah as sk\n" +
            "INNER JOIN presensi_dosen as pd on sk.id_presensi_dosen = pd.id\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.waktu_masuk <= CURDATE()\n" +
            "and pd.status_presensi in ('TERLAMBAT','BELUM_MENGAJAR')", nativeQuery = true)
    Object jmlKelasTerlambat(TahunAkademik tahunAkademik);

    @Query(value = "SELECT pd.id, pd.id_tahun_akademik, pd.id_jadwal, pd.waktu_masuk, pd.waktu_selesai,pd.status_presensi, pd.status, pd.id_dosen,kr.nama_karyawan as dosen,pr.nama_prodi as namaProdi, \n" +
            "jd.jam_mulai, jd.jam_selesai, sum(if(pd.status_presensi = 'HADIR', 1, 0)) as sesiTepatWaktu, COUNT(pd.id) as semuaSesi, round((sum(if(pd.status_presensi = 'HADIR', 1, 0)) * 100)/count(pd.id),2) as persentase\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF'\n" +
            "group by pd.id_dosen\n" +
            "having count(pd.id) >= 32\n" +
            "order by persentase DESC,sesiTepatWaktu DESC", countQuery = "SELECT COUNT(id) as jml from (select pd.id as id\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and pd.status = 'AKTIF'\n" +
            "group by pd.id_dosen\n" +
            "having count(pd.id) >= 32) as a", nativeQuery = true)
    Page<KlasemenDosenIntDto> klasemenDosen(TahunAkademik tahunAkademik, Pageable pageable);

    @Query(value = "SELECT pd.id, pd.id_tahun_akademik, pd.id_jadwal, pd.waktu_masuk, pd.waktu_selesai,pd.status_presensi, pd.status, pd.id_dosen,kr.nama_karyawan as dosen,pr.nama_prodi as namaProdi, \n" +
            "jd.jam_mulai, jd.jam_selesai, sum(if(pd.status_presensi = 'HADIR', 1, 0)) as sesiTepatWaktu, COUNT(pd.id) as semuaSesi, round((sum(if(pd.status_presensi = 'HADIR', 1, 0)) * 100)/count(pd.id),2) as persentase\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and kr.nama_karyawan LIKE %?2% and pd.status = 'AKTIF'\n" +
            "group by pd.id_dosen\n" +
            "having count(pd.id) >= 32\n" +
            "order by persentase DESC,sesiTepatWaktu DESC", countQuery = "SELECT COUNT(id) as jml from (select pd.id as id\n" +
            "FROM presensi_dosen as pd\n" +
            "inner join jadwal as jd on pd.id_jadwal = jd.id\n" +
            "inner join dosen as ds on pd.id_dosen = ds.id\n" +
            "inner join karyawan as kr on ds.id_karyawan = kr.id\n" +
            "inner join prodi as pr on jd.id_prodi = pr.id\n" +
            "where pd.id_tahun_akademik = ?1 and kr.nama_karyawan LIKE %?2% and pd.status = 'AKTIF'\n" +
            "group by pd.id_dosen\n" +
            "having count(pd.id) >= 32) as a", nativeQuery = true)
    Page<KlasemenDosenIntDto> klasemenDosenByTahunDanSearch(TahunAkademik tahunAkademik, String dosen, Pageable pageable);

    @Query(value = "select jadwal, dosen, nama, email, matkul, kelas, sks, bulan, hari,  group_concat(if(presensi is not null,date,null) order by date) as tanggal,\n" +
            "group_concat(if(presensi is null,date,null) order by date) as abstain, sum(if(presensi is not null,1,0)) as totalsesi,\n" +
            "sum(if(presensi is not null,sks,0)) as totalsks, sum(1) as totalseluruhsesi, sum(sks) as totalseluruhsks, id_prodi as idprodi, nama_prodi as prodi,\n" +
            "id_jenjang as idjenjang, jenjang, sdosen from\n" +
            "(select a.*,b.id as presensi from\n" +
            "(select c.id_presensi_dosen as id_presensi, b.id as jadwal, d.id as dosen, e.nama_karyawan as nama, e.email, nama_matakuliah as matkul,nama_kelas as kelas, jumlah_sks as sks,\n" +
            "monthname(c.waktu_mulai) as bulan, dayname(c.waktu_mulai) as hari,concat(day(waktu_mulai),'-',left(monthname(waktu_mulai),3)) as date,\n" +
            "h.id_prodi,i.nama_prodi,i.id_jenjang,nama_jenjang as jenjang, status_dosen as sdosen from jadwal_dosen as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join sesi_kuliah as c on b.id = c.id_jadwal\n" +
            "inner join dosen as d on a.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "inner join matakuliah_kurikulum as f on b.id_matakuliah_kurikulum = f.id\n" +
            "inner join matakuliah as g on f.id_matakuliah = g.id\n" +
            "inner join kelas as h on b.id_kelas = h.id\n" +
            "inner join prodi as i on h.id_prodi = i.id\n" +
            "inner join jenjang as j on i.id_jenjang = j.id\n" +
            "where date(c.tanggal_input) >= ?1 and date(c.tanggal_input) <= ?2) as a\n" +
            "left join presensi_dosen as b on a.id_presensi = b.id and a.dosen = b.id_dosen and b.status_presensi in ('HADIR','TERLAMBAT')) as a\n" +
            "group by jadwal, dosen", nativeQuery = true)
    List<SalaryDto> rekapSalary(LocalDate masuk, LocalDate selesai);

    @Query(value = "select a.id as id, g.nama_karyawan as dosen, f.nama_matakuliah as matakuliah, i.nama_kelas as kelas, h.nama as detailPertemuan from presensi_dosen as a \n" +
            "\tinner join sesi_kuliah as b on a.id = b.id_presensi_dosen\n" +
            "    inner join dosen as c on c.id = a.id_dosen\n" +
            "    inner join jadwal as d on d.id = a.id_jadwal\n" +
            "    inner join matakuliah_kurikulum as e on e.id = d.id_matakuliah_kurikulum\n" +
            "    inner join matakuliah as f on f.id = e.id_matakuliah\n" +
            "    inner join karyawan as g on g.id = c.id_karyawan\n" +
            "    left join detail_pertemuan h on h.id = b.id_detail_pertemuan\n" +
            "    inner join kelas as i on d.id_kelas = i.id\n" +
            "    where a.status = 'AKTIF' and d.id_tahun_akademik = ?1 and d.id_prodi = ?2 \n" +
            "    and b.pertemuan_ke = ?3 order by g.nama_karyawan ASC", nativeQuery = true)
    List<ReportUjianDto> reportUjian(TahunAkademik tahunAkademik, Prodi prodi, String jenis);

    @Query(value = "select id,dosen,'2024-03-01' as dari, '2024-03-31' as sampai,  terlambat, concat(coalesce(round((terlambat * 100)/total,0),0),'%') as persentase_terlambat,\n" +
            "tepat_waktu,concat(coalesce(round((tepat_waktu * 100)/total,0),0),'%') as persentase_tepat_waktu, total  from\n" +
            "(select id,dosen, sum(if(statuss = 'AKTIF',1,0)) as total, sum(if(status = 'TERLAMBAT',1,0)) as terlambat,  sum(if(status = 'TEPAT_WAKTU',1,0)) as tepat_waktu from\n" +
            "(select id,nama_karyawan as dosen, rangewaktu,statuss, if(statuss = 'AKTIF',if(rangewaktu > '00:15:00','TERLAMBAT','TEPAT_WAKTU'),'BELUM MULAI') as status from\n" +
            "(select d.id,e.nama_karyawan, c.jam_mulai,b.status as statuss, a.waktu_mulai + interval 7 hour as waktu_mulai, \n" +
            "TIMEDIFF(b.waktu_masuk + interval 7 hour,STR_TO_DATE(concat(date(b.waktu_masuk),' ',c.jam_mulai), '%Y-%m-%d %T')) as rangewaktu \n" +
            "from sesi_kuliah as a\n" +
            "inner join presensi_dosen as b on a.id_presensi_dosen = b.id\n" +
            "inner join jadwal as c on a.id_jadwal = c.id\n" +
            "inner join dosen as d on b.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "where b.status='AKTIF' and date(a.waktu_mulai) >= ?1 and date(a.waktu_mulai) <= ?2)as a)\n" +
            "as a group by dosen) as a", nativeQuery = true)
    List<Object[]> rekapPresensiDosen(LocalDate dari, LocalDate sampai);

    @Query(value = "select nama_karyawan as dosen, nama_matakuliah, nama_kelas,jam_mulai,waktu_mulai, rangewaktu, if(rangewaktu > '00:15:00','TERLAMBAT','TEPAT_WAKTU') as status from\n" +
            "(select e.nama_karyawan,nama_matakuliah, nama_kelas, STR_TO_DATE(concat(date(b.waktu_masuk),' ',c.jam_mulai), '%Y-%m-%d %T') as jam_mulai, a.waktu_mulai + interval 7 hour as waktu_mulai, \n" +
            "TIMEDIFF(b.waktu_masuk + interval 7 hour,STR_TO_DATE(concat(date(b.waktu_masuk),' ',c.jam_mulai), '%Y-%m-%d %T')) as rangewaktu \n" +
            "from sesi_kuliah as a\n" +
            "inner join presensi_dosen as b on a.id_presensi_dosen = b.id\n" +
            "inner join jadwal as c on a.id_jadwal = c.id\n" +
            "inner join dosen as d on b.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "inner join matakuliah_kurikulum as f on c.id_matakuliah_kurikulum = f.id\n" +
            "inner join matakuliah as g on f.id_matakuliah = g.id\n" +
            "inner join kelas as h on c.id_kelas = h.id\n" +
            "where b.status='AKTIF' and d.id = ?1 and date(a.waktu_mulai) >= ?2 and date(a.waktu_mulai) <= ?3)as a\n" +
            "order by waktu_mulai", nativeQuery = true)
    List<Object[]> detailPresensiDosen(String idDosen, LocalDate dari, LocalDate sampai);

    @Query(value = "select terlambat, concat(round((terlambat * 100)/total,0),'%') as persentase_terlambat,\n" +
            "tepat_waktu,concat(round((tepat_waktu * 100)/total,0),'%') as persentase_tepat_waktu, total  from\n" +
            "(select id,dosen, count(dosen) as total, sum(if(status = 'TERLAMBAT',1,0)) as terlambat,  sum(if(status = 'TEPAT_WAKTU',1,0)) as tepat_waktu from\n" +
            "(select id,nama_karyawan as dosen, rangewaktu, if(rangewaktu > '00:15:00','TERLAMBAT','TEPAT_WAKTU') as status from\n" +
            "(select d.id,e.nama_karyawan, c.jam_mulai, a.waktu_mulai + interval 7 hour as waktu_mulai, \n" +
            "TIMEDIFF(b.waktu_masuk + interval 7 hour,STR_TO_DATE(concat(date(b.waktu_masuk),' ',c.jam_mulai), '%Y-%m-%d %T')) as rangewaktu \n" +
            "from sesi_kuliah as a\n" +
            "inner join presensi_dosen as b on a.id_presensi_dosen = b.id\n" +
            "inner join jadwal as c on a.id_jadwal = c.id\n" +
            "inner join dosen as d on b.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "where b.status='AKTIF' and date(a.waktu_mulai) >= ?1 and date(a.waktu_mulai) <= ?2)as a)\n" +
            "as a) as a", nativeQuery = true)
    Object persentaseKehadiranDosen(LocalDate dari, LocalDate sampai);

}
