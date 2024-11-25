package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.api.DosenAktifSemesterDto;
import id.ac.tazkia.smilemahasiswa.dto.jadwaldosen.DetailDosen;
import id.ac.tazkia.smilemahasiswa.dto.jadwaldosen.DosenMengajar;
import id.ac.tazkia.smilemahasiswa.dto.machine.JadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapDosenDto;
import id.ac.tazkia.smilemahasiswa.dto.report.RekapJadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.validation.Valid;
import java.time.LocalTime;
import java.util.List;

public interface JadwalDosenDao extends PagingAndSortingRepository<JadwalDosen,String> {
    JadwalDosen findByJadwalAndStatusJadwalDosen(Jadwal jadwal, StatusJadwalDosen statusJadwalDosen);
    List<JadwalDosen> findByStatusJadwalDosenAndJadwal(StatusJadwalDosen statusJadwalDosen,Jadwal jadwal);

    List<JadwalDosen> findByJadwal(Jadwal jadwal);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.machine.JadwalDosenDto(j.dosen.id,j.dosen.absen,j.dosen.karyawan.namaKaryawan, j.dosen.karyawan.rfid,j.jadwal.id,j.jadwal.jamMulai,j.jadwal.jamSelesai,0) from JadwalDosen j where j.jadwal.ruangan = :ruangan and j.jadwal.tahunAkademik = :tahunAkademik and j.jadwal.hari = :hari and j.jadwal.status = :status and j.jadwal.jamMulai between :mulai and :sampai ")
    Iterable<JadwalDosenDto> cariJadwal(@Param("tahunAkademik") TahunAkademik ta, @Param("ruangan") Ruangan r, @Param("hari") Hari hari, @Param("status")StatusRecord statusRecord, @Param("mulai")LocalTime mulai, @Param("sampai") LocalTime sampai);

    @Query("select j from JadwalDosen  j where j.dosen = :dosen and j.jadwal.tahunAkademik = :tahun and j.jadwal.hari =:hari and j.jadwal.ruangan = :ruangan and  :sampai between  subtime(j.jadwal.jamMulai,'500') and subtime(j.jadwal.jamSelesai,'600')")
    JadwalDosen cari(@Param("dosen")Dosen dosen, @Param("tahun") TahunAkademik tahunAkademik, @Param("hari")Hari hari, @Param("ruangan") Ruangan ruangan,@Param("sampai")LocalTime sampai);

    Iterable<JadwalDosen> findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNullOrderByJadwalHariAscJadwalJamMulaiAsc(List<StatusRecord >hapus, TahunAkademik tahunAkademik, Dosen dosen);

    @Query(value = "select a.*, concat(coalesce(hadir, '0'), ' / ', total) as jmlPresensi from\n" +
            "(select j.id, h.id as idHari, j.jam_mulai, r.nama_ruangan, concat(TIME_FORMAT(j.jam_mulai, '%H:%i'), ' - ', TIME_FORMAT(j.jam_selesai, '%H:%i')) as Jam, m.nama_matakuliah_english, k.nama_kelas, h.nama_hari, mk.jumlah_sks, kr.nama_karyawan, jd.status_jadwal_dosen from jadwal_dosen jd inner join jadwal j on jd.id_jadwal=j.id inner join dosen d on jd.id_dosen=d.id inner join ruangan r on j.id_ruangan=r.id inner join matakuliah_kurikulum mk on j.id_matakuliah_kurikulum=mk.id inner join matakuliah m on mk.id_matakuliah=m.id inner join kelas k on j.id_kelas=k.id inner join karyawan kr on d.id_karyawan=kr.id inner join hari h on j.id_hari=h.id where d.id=?1 and j.id_tahun_akademik=?2 and j.status not in('HAPUS')) as a\n" +
            "left join\n" +
            "(select id_jadwal, count(id) as hadir from sesi_kuliah where pertemuan<>'Belum Mengajar' group by id_jadwal) as b on a.id=b.id_jadwal\n" +
            "left join\n" +
            "(select id_jadwal, count(id) as total from sesi_kuliah group by id_jadwal) as c on a.id=c.id_jadwal\n" +
            "order by a.idHari, a.jam_mulai", nativeQuery = true)
    List<Object[]> listJadwalMengajarDashboardDosen(String idDosen, String idTahunAkademik);

    static final String REKAP_JADWAL_DOSEN = "select new id.ac.tazkia.smilemahasiswa.dto.report.RekapJadwalDosenDto(jd.jadwal.id,jd.dosen.id,jd.dosen.karyawan.namaKaryawan, jd.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, jd.jadwal.matakuliahKurikulum.jumlahSks, jd.jadwal.prodi.namaProdi, jd.jadwal.kelas.namaKelas, jd.jadwal.hari.namaHari,jd.jadwal.sesi, jd.jadwal.jamMulai, jd.jadwal.jamSelesai, jd.jadwal.ruangan.namaRuangan, jd.jadwal.ruangan.gedung.namaGedung, jd.jumlahKehadiran) from JadwalDosen jd where jd.statusJadwalDosen = :statusJadwalDosen and jd.jadwal.tahunAkademik = :ta and jd.jadwal.status = :statusJadwal order by jd.dosen.karyawan.namaKaryawan, jd.jadwal.hari.id, jd.jadwal.jamMulai";

    @Query(REKAP_JADWAL_DOSEN)
    Page<RekapJadwalDosenDto> rekapJadwalDosen(@Param("statusJadwalDosen")StatusJadwalDosen sjd, @Param("ta")TahunAkademik ta, @Param("statusJadwal")StatusRecord statusJadwal, Pageable page);

    @Query(value = "select c.id,d.nama_karyawan as nama,c.status_dosen as status,f.nama_matakuliah as matkul ,g.nama_kelas as kelas,group_concat(DAYOFMONTH(waktu_masuk))as tanggal,e.jumlah_sks as sks,count(a.id)as hadir from presensi_dosen as a inner join jadwal as b on a.id_jadwal=b.id inner join dosen as c on a.id_dosen=c.id inner join karyawan as d on c.id_karyawan=d.id inner join matakuliah_kurikulum as e on b.id_matakuliah_kurikulum=e.id inner join matakuliah as f on e.id_matakuliah=f.id inner join kelas as g on b.id_kelas = g.id where year(waktu_masuk)=?1 and month(waktu_masuk)=?2 and a.status='AKTIF' group by a.id_dosen,a.id_jadwal order by d.nama_karyawan", nativeQuery = true)
    List<RekapDosenDto> rekapDosen(Integer tahun,Integer bulan);

    @Query(value = "SELECT GROUP_CONCAT(h.nama_karyawan, ' / ')AS dosen  \n" +
            "FROM jadwal_dosen AS a \n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "LEFT JOIN prodi AS c ON b.id_prodi = c.id\n" +
            "LEFT JOIN matakuliah_kurikulum AS d ON b.id_matakuliah_kurikulum = d.id\n" +
            "LEFT JOIN matakuliah AS e ON d.id_matakuliah = e.id \n" +
            "LEFT JOIN kelas AS f ON b.id_kelas = f.id\n" +
            "LEFT JOIN dosen AS g ON a.id_dosen = g.id\n" +
            "LEFT JOIN karyawan AS h ON g.id_karyawan = h.id\n" +
            "WHERE a.id_jadwal = ?1", nativeQuery = true)
    String headerJadwal(String idJadwal);

    List<JadwalDosen> findByJadwalTahunAkademik(TahunAkademik tahunAkademik);


    @Query(value = "select kode_prodi as kodeProdi, sum(if(status_dosen<>'HB',1,0)) as lb, sum(if(status_dosen='HB',1,0)) as hb, count(kode_prodi) as total from\n" +
            "(select d.kode_prodi, a.id_dosen, e.status_dosen  from jadwal_dosen as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join dosen as e on a.id_dosen = e.id\n" +
            "inner join prodi as d on b.id_prodi = d.id\n" +
            "where c.tanggal_mulai <= date(now()) and c.tanggal_selesai_nilai >= date(now()) \n" +
            "and b.status = 'AKTIF' and c.status = 'AKTIF' and d.status = 'AKTIF' and e.status = 'AKTIF'\n" +
            "group by b.id_prodi, a.id_dosen) as a \n" +
            "group by kode_prodi order by count(kode_prodi) desc", nativeQuery = true)
    List<DosenAktifSemesterDto> cariDosenAktifSemester();

    @Query(value = "SELECT k.email as email,j.jam_mulai as mulai ,j.jam_selesai as selesai, m.nama_matakuliah as matakuliah, ke.nama_kelas as kelas, jd.status_jadwal_dosen as jenis " +
            "FROM jadwal as j inner join jadwal_dosen as jd on j.id = jd.id_jadwal inner join dosen as d on jd.id_dosen = d.id inner join karyawan as k on d.id_karyawan = k.id inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id" +
            " inner join matakuliah as m on mk.id_matakuliah = m.id inner join kelas as ke on j.id_kelas = ke.id" +
            " where j.id_tahun_akademik = ?1 and j.status = 'AKTIF' and j.id_hari is not null and j.jam_mulai is not null and jd.id_dosen = ?2 and m.singkatan not in ('SDS')", nativeQuery = true)
    List<DetailDosen> getJadwalDosen(TahunAkademik tahunAkademik, Dosen dosen);

    @Query(value = "SELECT\n" +
            "       d.id,\n" +
            "       k.nama_karyawan AS dosen,\n" +
            "       k.jenis_kelamin as gender\n" +
            "FROM jadwal AS j \n" +
            "INNER JOIN jadwal_dosen AS jd ON j.id = jd.id_jadwal \n" +
            "INNER JOIN dosen AS d ON jd.id_dosen = d.id \n" +
            "INNER JOIN karyawan AS k ON d.id_karyawan = k.id \n" +
            "INNER JOIN matakuliah_kurikulum AS mk ON j.id_matakuliah_kurikulum = mk.id \n" +
            "INNER JOIN matakuliah AS m ON mk.id_matakuliah = m.id \n" +
            "INNER JOIN kelas AS ke ON j.id_kelas = ke.id \n" +
            "WHERE j.id_tahun_akademik = ?1 \n" +
            "  AND j.status = 'AKTIF' \n" +
            "  AND j.id_hari IS NOT NULL \n" +
            "  AND j.jam_mulai IS NOT NULL\n" +
            "GROUP BY jd.id_dosen, k.nama_karyawan\n" +
            "ORDER by k.nama_karyawan ASC \n", nativeQuery = true)
    List<DosenMengajar> listDosenMengajar(TahunAkademik tahunAkademik);

}
