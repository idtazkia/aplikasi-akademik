package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.KrsNilaiTugasDto;
import id.ac.tazkia.smilemahasiswa.dto.report.DataKhsDto;
import id.ac.tazkia.smilemahasiswa.dto.report.EdomDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.StudentDto;
import id.ac.tazkia.smilemahasiswa.dto.transkript.DataTranskript;
import id.ac.tazkia.smilemahasiswa.dto.transkript.TranskriptDto;
import id.ac.tazkia.smilemahasiswa.dto.user.IpkDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.hibernate.sql.Update;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail, String> {
    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.hari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.hari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query(value = "select aaaa.id,bbbb.nama_hari,cccc.nama_kelas,aaaa.jam_mulai,aaaa.jam_selesai,aaaa.sks,eeee.nama_karyawan as dosen," +
            "aaaa.nama_matakuliah,aaaa.kapasitas,'0' as mhsw,cmkk,bmkk as bmk,prass,matkul_prass as bmkk,nama_prass, " +
            "aaaa.nama_matakuliah_english from (select aaa.*,bbb.id_jadwal as cmkk,ccc.kode_matakuliah bmkk,ddd.kode_matakuliah as prass," +
            "eee.kode_matakuliah as matkul_prass,eee.nama_matakuliah as nama_prass from(select * from(select a.*,b.id_matakuliah," +
            "c.kode_matakuliah,b.jumlah_sks as sks,c.nama_matakuliah,c.nama_matakuliah_english from jadwal as a " +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='TERTUTUP' " +
            "and a.id_kelas=?2 and a.id_hari is not null union select a.*,b.id_matakuliah,c.kode_matakuliah,b.jumlah_sks as sks," +
            "c.nama_matakuliah,c.nama_matakuliah_english from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='PRODI' " +
            "and a.id_prodi=?3 and a.id_hari is not null union select a.*,b.id_matakuliah,c.kode_matakuliah,b.jumlah_sks as sks," +
            "c.nama_matakuliah,c.nama_matakuliah_english from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.akses='UMUM' " +
            "and a.id_hari is not null group by a.id)aa)as aaa left join(select a.id_jadwal,a.id_matakuliah_kurikulum,b.id_matakuliah," +
            "c.kode_matakuliah,c.nama_matakuliah,c.nama_matakuliah_english from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF' and a.id_mahasiswa=?4 " +
            "group by a.id_jadwal)bbb on aaa.id=bbb.id_jadwal or aaa.id_matakuliah_kurikulum=bbb.id_matakuliah_kurikulum or " +
            "aaa.id_matakuliah=bbb.id_matakuliah or aaa.kode_matakuliah=bbb.kode_matakuliah or aaa.nama_matakuliah=bbb.nama_matakuliah " +
            "left join(select a.id_matakuliah_kurikulum,b.id_matakuliah,c.kode_matakuliah,c.nama_matakuliah from krs_detail as a " +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id " +
            "where a.status='AKTIF' and a.bobot >= 3.00 and a.id_mahasiswa=?4 group by id_jadwal)ccc on " +
            "aaa.id_matakuliah_kurikulum=ccc.id_matakuliah_kurikulum or aaa.id_matakuliah=ccc.id_matakuliah or " +
            "aaa.kode_matakuliah=ccc.kode_matakuliah left join(select a.*,c.kode_matakuliah,c.nama_matakuliah " +
            "from prasyarat as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum_pras=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.status='AKTIF')eee on " +
            "aaa.id_matakuliah_kurikulum=eee.id_matakuliah_kurikulum left join(select aa.* from(select a.*,b.id_matakuliah," +
            "c.kode_matakuliah,c.nama_matakuliah from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id where a.status='AKTIF' and a.id_mahasiswa=?4)aa " +
            "inner join(select a.*,c.kode_matakuliah,c.nama_matakuliah from prasyarat as a inner join matakuliah_kurikulum as b " +
            "on a.id_matakuliah_kurikulum_pras=b.id inner join matakuliah as c on a.id_matakuliah_pras=c.id where a.status='AKTIF' " +
            "group by c.kode_matakuliah)bb on aa.id_matakuliah_kurikulum=bb.id_matakuliah_kurikulum_pras or " +
            "aa.id_matakuliah=bb.id_matakuliah_pras or aa.kode_matakuliah=bb.kode_matakuliah where aa.bobot >= bb.nilai " +
            "group by aa.id)ddd on eee.id_matakuliah_kurikulum = ddd.id_matakuliah_kurikulum or eee.id_matakuliah=ddd.id_matakuliah " +
            "or eee.kode_matakuliah=ddd.kode_matakuliah where bbb.id_jadwal is null and ccc.kode_matakuliah is null)aaaa " +
            "inner join hari as bbbb on aaaa.id_hari=bbbb.id inner join kelas as cccc on aaaa.id_kelas=cccc.id " +
            "inner join dosen as dddd on aaaa.id_dosen_pengampu=dddd.id inner join karyawan as eeee on dddd.id_karyawan=eeee.id " +
            "group by aaaa.id", nativeQuery = true)
    List<Object[]> pilihanKrs(TahunAkademik tahunAkademik, Kelas kelas, Prodi prodi, Mahasiswa mahasiswa);

    @Query(value = "SELECT * FROM\n" +
            "(SELECT a.id AS id_jadwal, a.id_number_elearning,hr.nama_hari, a.jam_mulai, a.jam_selesai,d.kode_matakuliah, d.nama_matakuliah_english, d.nama_matakuliah,c.jumlah_sks, g.nama_kelas, f.nama_karyawan AS dosen,\n" +
            "i.nama_matakuliah AS prasyarat, h.nilai AS nilai_prasyarat,\n" +
            "j.kode_matakuliah AS pras_sudah_diambil,j.bobot AS nilai_pras_sudah_diambil,\n" +
            "k.kode_matakuliah AS sudah_diambil_semester_sebelumnya, k.bobot AS nilai_sudah_diambil_semester_sebelumnya,\n" +
            "l.kode_matakuliah AS sudah_diambil_semester_ini, l.bobot AS nilai_sudah_diambil_semester_ini\n" +
            "FROM \n" +
            "(select * from jadwal where id_tahun_akademik = ?1 and id_hari IS NOT NULL and jam_mulai IS NOT NULL and jam_selesai IS NOT NULL and status = 'AKTIF' and akses = 'TERTUTUP' and id_kelas = ?2\n" +
            "union\n" +
            "select * from jadwal where id_tahun_akademik = ?1  and id_hari IS NOT NULL and jam_mulai IS NOT NULL and jam_selesai IS NOT NULL and status = 'AKTIF' and akses = 'PRODI' and id_prodi = ?3\n" +
            "union\n" +
            "select * from jadwal where id_tahun_akademik = ?1  and id_hari IS NOT NULL and jam_mulai IS NOT NULL and jam_selesai IS NOT NULL and status = 'AKTIF' and akses = 'UMUM') \n" +
            " AS a \n" +
            "INNER JOIN jadwal_dosen AS b ON a.id = b.id_jadwal\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON a.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "INNER JOIN dosen AS e ON b.id_dosen = e.id\n" +
            "INNER JOIN karyawan AS f ON e.id_karyawan = f.id\n" +
            "INNER JOIN kelas AS g ON a.id_kelas = g.id\n" +
            "INNER JOIN hari AS hr ON a.id_hari = hr.id\n" +
            "LEFT JOIN prasyarat AS h ON d.id = h.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS i ON h.id_matakuliah_pras = i.id\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik <> ?1 AND v.id_mahasiswa = ?4\n" +
            ") j ON i.id = j.id OR i.kode_matakuliah = j.kode_matakuliah OR i.nama_matakuliah = j.nama_matakuliah OR i.id = j.id_matakuliah_setara OR i.nama_matakuliah = j.nama_matakuliah_setara OR i.kode_matakuliah = j.kode_matakuliah_setara\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik <> ?1 AND v.id_mahasiswa = ?4\n" +
            ") k ON d.id = j.id OR d.kode_matakuliah = j.kode_matakuliah OR d.nama_matakuliah = j.nama_matakuliah OR d.id = j.id_matakuliah_setara OR d.nama_matakuliah = j.nama_matakuliah_setara OR d.kode_matakuliah = j.kode_matakuliah_setara\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik = ?1 AND v.id_mahasiswa = ?4\n" +
            ")l ON d.id = l.id OR d.kode_matakuliah = l.kode_matakuliah OR d.nama_matakuliah = l.nama_matakuliah OR d.id = l.id_matakuliah_setara OR d.nama_matakuliah = l.nama_matakuliah_setara OR d.kode_matakuliah = l.kode_matakuliah_setara\n" +
            "where b.status_jadwal_dosen = 'PENGAMPU'\n" +
            "GROUP BY a.id\n" +
            "ORDER BY d.nama_matakuliah_english, g.nama_kelas, f.nama_karyawan)AS a WHERE (nilai_pras_sudah_diambil >= nilai_prasyarat OR nilai_prasyarat IS NULL) AND sudah_diambil_semester_ini IS NULL", nativeQuery = true)
    List<Object[]> pilihKrs(TahunAkademik tahunAkademik,Kelas kelas, Prodi prodi, Mahasiswa mahasiswa);

    @Query(value = "SELECT * FROM\n" +
            "(SELECT a.id AS id_jadwal, a.id_number_elearning, hr.nama_hari, a.jam_mulai, a.jam_selesai, d.kode_matakuliah, d.nama_matakuliah_english, d.nama_matakuliah, g.nama_kelas,c.jumlah_sks, f.nama_karyawan AS dosen,\n" +
            "i.nama_matakuliah AS prasyarat, h.nilai AS nilai_prasyarat,\n" +
            "j.kode_matakuliah AS pras_sudah_diambil,j.bobot AS nilai_pras_sudah_diambil,\n" +
            "k.kode_matakuliah AS sudah_diambil_semester_sebelumnya, k.bobot AS nilai_sudah_diambil_semester_sebelumnya,\n" +
            "l.kode_matakuliah AS sudah_diambil_semester_ini, l.bobot AS nilai_sudah_diambil_semester_ini\n" +
            "FROM jadwal AS a \n" +
            "INNER JOIN jadwal_dosen AS b ON a.id = b.id_jadwal\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON a.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "INNER JOIN dosen AS e ON b.id_dosen = e.id\n" +
            "INNER JOIN karyawan AS f ON e.id_karyawan = f.id\n" +
            "INNER JOIN kelas AS g ON a.id_kelas = g.id\n" +
            "INNER JOIN hari as hr ON a.id_hari = hr.id\n" +
            "LEFT JOIN prasyarat AS h ON d.id = h.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS i ON h.id_matakuliah_pras = i.id\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik <> ?1 AND v.id_mahasiswa = ?3\n" +
            ") j ON i.id = j.id OR i.kode_matakuliah = j.kode_matakuliah OR i.nama_matakuliah = j.nama_matakuliah OR i.id = j.id_matakuliah_setara OR i.nama_matakuliah = j.nama_matakuliah_setara OR i.kode_matakuliah = j.kode_matakuliah_setara\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik <> ?1 AND v.id_mahasiswa = ?3\n" +
            ") k ON d.id = j.id OR d.kode_matakuliah = j.kode_matakuliah OR d.nama_matakuliah = j.nama_matakuliah OR d.id = j.id_matakuliah_setara OR d.nama_matakuliah = j.nama_matakuliah_setara OR d.kode_matakuliah = j.kode_matakuliah_setara\n" +
            "LEFT JOIN\n" +
            "(SELECT a.bobot, d.id,d.kode_matakuliah,d.nama_matakuliah, e.id_matakuliah_setara, f.kode_matakuliah AS kode_matakuliah_setara, f.nama_matakuliah AS nama_matakuliah_setara FROM\n" +
            "krs_detail AS a\n" +
            "INNER JOIN krs AS v ON a.id_krs = v.id\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN matakuliah_kurikulum AS c ON b.id_matakuliah_kurikulum = c.id\n" +
            "INNER JOIN matakuliah AS d ON c.id_matakuliah = d.id\n" +
            "LEFT JOIN matakuliah_setara AS e ON d.id = e.id_matakuliah\n" +
            "LEFT JOIN matakuliah AS f ON e.id_matakuliah_setara = f.id\n" +
            "WHERE a.status = 'AKTIF' AND v.id_tahun_akademik = ?1 AND v.id_mahasiswa = ?3\n" +
            ")l ON d.id = l.id OR d.kode_matakuliah = l.kode_matakuliah OR d.nama_matakuliah = l.nama_matakuliah OR d.id = l.id_matakuliah_setara OR d.nama_matakuliah = l.nama_matakuliah_setara OR d.kode_matakuliah = l.kode_matakuliah_setara\n" +
            "WHERE a.id_tahun_akademik = ?1 AND a.status = 'AKTIF' AND b.status_jadwal_dosen = 'PENGAMPU' AND a.akses IN ('prodi','umum') AND a.id_prodi = ?2\n" +
            "GROUP BY a.id\n" +
            "ORDER BY d.nama_matakuliah_english, g.nama_kelas, f.nama_karyawan)AS a WHERE (nilai_pras_sudah_diambil >= nilai_prasyarat OR nilai_prasyarat IS NULL) AND sudah_diambil_semester_ini IS NULL", nativeQuery = true)
    List<Object[]> pilihKrsMahasiswa(TahunAkademik tahunAkademik, Prodi prodi, Mahasiswa mahasiswa);

    @Query("select sum (kd.matakuliahKurikulum.jumlahSks) from KrsDetail kd where kd.status = :status and kd.krs= :krs")
    Long jumlahSks (@Param("status") StatusRecord statusRecord,@Param("krs")Krs krs);

    @Query(value = "select e.nama_matakuliah, count(c.id) as total, round(((count(c.id)*100)/3),2) as persentase  from (select * from krs_detail where status='AKTIF' and id_mahasiswa=?1)a inner join (select * from krs where status='AKTIF' and id_mahasiswa=?1 and id_tahun_akademik=?2)b on a.id_krs = b.id inner join (select aa.* from presensi_mahasiswa as aa inner join sesi_kuliah as bb on aa.id_sesi_kuliah = bb.id inner join presensi_dosen as cc on bb.id_presensi_dosen = cc.id where cc.status='AKTIF' and aa.id_mahasiswa = ?1 and aa.status = 'AKTIF' and aa.status_presensi in ('MANGKIR','TERLAMBAT'))c on a.id = c.id_krs_detail inner join matakuliah_kurikulum as d on a.id_matakuliah_kurikulum=d.id inner join matakuliah as e on d.id_matakuliah=e.id group by id_krs_Detail", nativeQuery = true)
    List<Object[]> persentaseKehadiran(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatusAndStatusEdom(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord,StatusRecord statusEdom);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.report.EdomDto (kd.id,kd.jadwal.dosen.karyawan.namaKaryawan,kd.matakuliahKurikulum.matakuliah.namaMatakuliah) from KrsDetail kd where kd.mahasiswa = :mahasiswa and kd.krs.tahunAkademik = :akademik and kd.status = :status and kd.statusEdom = :edom")
    List<EdomDto>cariEdom(@Param("mahasiswa")Mahasiswa mahasiswa,@Param("akademik")TahunAkademik tahunAkademik,@Param("status")StatusRecord status,@Param("edom") StatusRecord edom);

    @Query(value = "select kd.id,m.kode_matakuliah as kode ,m.nama_matakuliah_english as matakuliah,kd.nilai_presensi as presensi ,kd.nilai_tugas as tugas,kd.nilai_uts as uts,kd.nilai_uas as uas,coalesce (kd.nilai_akhir,0) as nilaiAkhir,coalesce(kd.bobot,0)  as bobot,coalesce (kd.grade,'E') as grade, jumlah_sks as sks, jumlah_sks * kd.bobot as total from krs_detail as kd inner join matakuliah_kurikulum as mk on kd.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id where kd.id_mahasiswa = ?2 and kd.status = 'AKTIF' and kd.id_tahun_akademik = ?1", nativeQuery = true)
    List<DataKhsDto> getKhs(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.namaMatakuliah = :nama and kd.nilaiAkhir >= 55")
    List<BigDecimal> nilaiMagang(@Param("status")StatusRecord statusRecord, @Param("mahasiswa") Mahasiswa mahasiswa, @Param("nama")String nama);

    @Query("select kd.nilaiAkhir from KrsDetail kd where kd.status = :status and kd.mahasiswa = :mahasiswa and kd.matakuliahKurikulum.matakuliah.singkatan = :singkatan and kd.nilaiAkhir >= 55")
    List<BigDecimal> nilaiMetolit(@Param("status")StatusRecord statusRecord,@Param("mahasiswa") Mahasiswa mahasiswa,@Param("singkatan")String singkatan);

    @Query(value = "select aa.status_presensi, coalesce(aa.waktu_masuk,bb.waktu_mulai) as masuk, coalesce(aa.waktu_keluar,bb.waktu_selesai)as keluar, bb.berita_acara from (select a.* from presensi_mahasiswa as a inner join krs_detail as b on a.id_krs_detail=b.id inner join krs as c on b.id_krs=c.id where a.id_mahasiswa=?1 and c.id_tahun_akademik=?2 and a.status='AKTIF' and b.status='AKTIF' and c.status='AKTIF' and b.id_jadwal=?3)aa inner join sesi_kuliah as bb on aa.id_sesi_kuliah=bb.id inner join (select * from presensi_dosen where status='AKTIF' and id_jadwal=?3) cc on bb.id_presensi_dosen=cc.id order by bb.waktu_mulai", nativeQuery = true)
    List<Object[]> detailPresensi(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,Jadwal jadwal);

    @Query("select kd.id from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    String validasiKrs(@Param("jadwal") Jadwal jadwal);

    @Query("select count (kd) from KrsDetail kd where kd.matakuliahKurikulum = :matakuliah and kd.status = 'AKTIF'")
    Long hitungMatakuliahKurikulum(@Param("matakuliah") MatakuliahKurikulum matakuliahKurikulum);

    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal j, StatusRecord aktif, TahunAkademik byStatus);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal j, StatusRecord aktif);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNimAsc(Jadwal j, StatusRecord aktif);

    @Query("select kd from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF' and kd.krs.tahunAkademik = :tahun and kd.mahasiswa = :mahasiswa")
    KrsDetail cariKrs(@Param("jadwal")Jadwal jadwal,@Param("tahun")TahunAkademik tahun,@Param("mahasiswa")Mahasiswa mahasiswa);

    List<KrsDetail> findByJadwalAndStatus(Jadwal jadwal, StatusRecord aktif);

    @Query("select kd.id from KrsDetail kd where kd.mahasiswa.id = :id and kd.status = :status and kd.matakuliahKurikulum.matakuliah.namaMatakuliah like %:nama%")
    String idKrsDetail(@Param("id")String id,@Param("status")StatusRecord statusRecord,@Param("nama")String nama);

    KrsDetail findByMahasiswaAndJadwalAndStatus(Mahasiswa byNim, Jadwal jadwal, StatusRecord aktif);

    @Query("SELECT sum(kd.e1) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE1 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e2) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE2 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e3) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE3 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e4) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE4 (@Param("jadwal") Jadwal jadwal);

    @Query("SELECT sum(kd.e5) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF'")
    Long jumlahE5 (@Param("jadwal") Jadwal jadwal);

    List<KrsDetail> findByMahasiswaNotInAndJadwalAndStatus(List<Mahasiswa> mahasiswas, Jadwal jadwal,StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.StudentDto (kd.mahasiswa.nim,kd.mahasiswa.nama,kd.mahasiswa.idProdi.namaProdi) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = 'AKTIF' order by kd.mahasiswa.nim asc")
    List<StudentDto> cariJadwalMahasiswa(@Param("jadwal") Jadwal jadwal);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND a.nilai_akhir IS NOT NULL AND a.id_mahasiswa IS NOT NULL AND id_mahasiswa=?1" , nativeQuery = true)
    IpkDto ipk (Mahasiswa mahasiswa);

    @Query(value = "select round(sum(bobot*jumlah_sks)/sum(jumlah_sks),2) from (select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,max(nilai_akhir),bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester) t1" , nativeQuery = true)
    BigDecimal ipkTranskript (Mahasiswa mahasiswa);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*d.jumlah_sks)/SUM(d.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN krs AS b ON a.id_krs=b.id INNER JOIN jadwal AS c ON a.id_jadwal = c.id INNER JOIN matakuliah_kurikulum AS d ON c.id_matakuliah_kurikulum=d.id WHERE b.id_mahasiswa=?1 AND b.id_tahun_akademik=?2 and a.status = 'AKTIF' AND d.jumlah_sks > 0", nativeQuery = true)
    IpkDto ip (Mahasiswa mahasiswa, TahunAkademik tahun);


    KrsDetail findByJadwalAndStatusAndKrs(Jadwal j, StatusRecord aktif, Krs krs);

    Long countByJadwalAndStatus(Jadwal jadwal, StatusRecord statusRecord);

    @Query(value = "select a.* from (select a.id,a.id_mahasiswa,b.nim,b.nama,c.nama_prodi from krs as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id where a.id_tahun_akademik=?1 and a.status='AKTIF')a left join (select a.id,a.id_krs from krs_detail as a inner join krs as b on a.id_krs=b.id where id_jadwal=?2 and a.status='AKTIF' and b.id_tahun_akademik=?1 and b.status='AKTIF')b on a.id=b.id_krs  where b.id is null",nativeQuery = true)
    List<Object[]>cariMahasiswaJadwal(TahunAkademik tahunAkademik, Jadwal jadwal);

    @Query(value = "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkrip(Mahasiswa mahasiswa);

    @Query(value = "\n" +
            "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,\n" +
            "COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu \n" +
            "FROM krs_detail AS a \n" +
            "INNER JOIN jadwal AS g ON a.id_jadwal = g.id\n" +
            "INNER JOIN matakuliah_kurikulum AS b ON g.id_matakuliah_kurikulum=b.id \n" +
            "INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id \n" +
            "WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 AND b.semester =?2 ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkripSem(Mahasiswa mahasiswa, String semester);



    @Query(value="SELECT h.id, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,\n" +
            "            COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu ,kode_tahun_akademik, nama_tahun_akademik, b.id AS idMatKul \n" +
            "            FROM krs_detail AS a \n" +
            "            inner join krs as h on a.id_krs = h.id\n" +
            "            inner join tahun_akademik as i on h.id_tahun_akademik = i.id\n" +
            "            INNER JOIN jadwal AS g ON a.id_jadwal = g.id\n" +
            "            INNER JOIN matakuliah_kurikulum AS b ON g.id_matakuliah_kurikulum=b.id \n" +
            "            INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id \n" +
            "            WHERE a.status='AKTIF' AND a.id_mahasiswa=?1 AND b.jumlah_sks > 0", nativeQuery = true)
    List<Object[]> transkriptTampil(String idMahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='1' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='1' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem1(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='2' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='2' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem2(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='3' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='3' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem3(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='4' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='4' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem4(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='5' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='5' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem5(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='6' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='6' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem6(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='7' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='7' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem7(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='8' and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.semester='8' and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhirSem8(Mahasiswa mahasiswa);

    @Query(value = "select semester,kode_matakuliah,nama_matakuliah,jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu from ((select id_matakuliah,id_mahasiswa,max(nilai_akhir) as nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join jadwal as g on a.id_jadwal = g.id inner join matakuliah_kurikulum as b on g.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and b.jumlah_sks > 0  and bobot > 0 and a.status = 'AKTIF'  group by c.kode_matakuliah) union (select id_matakuliah,id_mahasiswa,nilai_akhir,bobot,a.id as id_krs,c.kode_matakuliah,c.nama_matakuliah,b.jumlah_sks,grade,semester from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id inner join matakuliah as c on b.id_matakuliah=c.id where a.id_mahasiswa=?1 and a.status='AKTIF' and jumlah_sks > 0))aa where grade <> 'E'  group by kode_matakuliah order by semester", nativeQuery = true)
    List<Object[]> transkripAKhir(Mahasiswa mahasiswa);

    @Query(value = "SELECT b.semester, c.kode_matakuliah, c.nama_matakuliah, b.jumlah_sks,COALESCE(a.bobot,'waiting')AS bobot,\n" +
            "COALESCE(a.grade,'waiting')AS grade,COALESCE((b.jumlah_sks*a.bobot),'waiting') AS mutu \n" +
            "FROM krs_detail AS a \n" +
            "INNER JOIN jadwal AS g ON a.id_jadwal = g.id\n" +
            "INNER JOIN matakuliah_kurikulum AS b ON g.id_matakuliah_kurikulum=b.id \n" +
            "INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id \n" +
            "WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 AND b.semester =?2 AND bobot IS NOT NULL ORDER BY b.semester", nativeQuery = true)
    List<Object[]> transkripSemesterWithoutWaiting(Mahasiswa mahasiswa, String semester);

    @Query(value = "SELECT c.nim,c.nama,d.nama_prodi,a.id_tahun_akademik,'A' AS STATUS,coalesce(e.sks_total,0) AS sks_semester,coalesce(e.ipk,0) AS ip_semester,coalesce(b.sks_total,0) as sks_total,coalesce(b.ipk ,0) as ipk FROM (SELECT a.* FROM krs as a inner join mahasiswa as b on a.id_mahasiswa=b.id WHERE a.STATUS='AKTIF' AND a.id_tahun_akademik=?1 and b.angkatan=?2 )a  LEFT JOIN (SELECT id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND b.jumlah_sks > 0 AND a.nilai_akhir IS NOT NULL AND a.id_mahasiswa IS NOT NULL GROUP BY a.id_mahasiswa)b ON a.id_mahasiswa=b.id_mahasiswa INNER JOIN mahasiswa AS c ON a.id_mahasiswa = c.id INNER JOIN prodi AS d ON c.id_prodi=d.id LEFT JOIN (SELECT id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b  ON a.id_matakuliah_kurikulum=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id WHERE a.status='AKTIF' AND id_tahun_akademik=?1 AND b.jumlah_sks > 0 AND a.id_mahasiswa IS NOT NULL and c.angkatan=?2 GROUP BY a.id_mahasiswa)e ON a.id_mahasiswa=e.id_mahasiswa ORDER BY d.kode_prodi, c.nim", nativeQuery = true)
    List<Object[]> cariIpk(TahunAkademik tahunAkademik,String angkatan);

    @Query(value = "SELECT sum(b.jumlah_sks) FROM krs_detail AS a inner join jadwal as g on a.id_jadwal = g.id INNER JOIN matakuliah_kurikulum AS b ON g.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    Long totalSks(Mahasiswa mahasiswa);

    @Query(value = "select coalesce(sum(jumlah_sks),0.00)as jumlah_sks from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots\n" +
            "order by kode_matakuliah", nativeQuery = true)
    BigDecimal totalSksAkhir(String mahasiswa);

    @Query(value = "SELECT sum(b.jumlah_sks*a.bobot) FROM krs_detail AS a inner join jadwal as g on a.id_jadwal = g.id INNER JOIN matakuliah_kurikulum AS b ON g.id_matakuliah_kurikulum=b.id INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id WHERE a.status='AKTIF' AND id_mahasiswa=?1 AND b.jumlah_sks > 0 ORDER BY b.semester", nativeQuery = true)
    Long totalMutu(Mahasiswa mahasiswa);

    @Query(value = "select coalesce(sum(mutu),0.00) as mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots\n" +
            "order by kode_matakuliah", nativeQuery = true)
    BigDecimal totalMutuAkhir(String mahasiswa);

    @Query(value = "select d.nim,d.nama,e.nama_prodi,f.nama_tahun_akademik,c.nama_matakuliah,a.kode_uts from krs_detail as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id inner join mahasiswa as d on a.id_mahasiswa=d.id inner join prodi as e on d.id_prodi=e.id inner join tahun_akademik as f on a.id_tahun_akademik=f.id left join (select count(a.id)as presensi,id_krs_detail from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah=b.id inner join presensi_dosen as c on b.id_presensi_dosen=c.id inner join jadwal as d on c.id_jadwal=d.id where a.id_mahasiswa=?1 and d.id_tahun_akademik=?2 and a.status_presensi in ('MANGKIR','TERLAMBAT') and a.status='AKTIF' group by d.id) g on a.id=g.id_krs_detail where a.id_mahasiswa=?1 and a.id_tahun_akademik=?2 and coalesce(presensi,0) < 4", nativeQuery = true)
    List<Object[]> listKartu(Mahasiswa mahasiswa,TahunAkademik tahunAkademik);

    @Query(value = "SELECT id_mahasiswa as nim,nama,kode_uas as kode FROM (SELECT a.*,d.nama,COALESCE(b.mangkir,0) AS mangkir,c.fitur FROM (SELECT * FROM krs_detail WHERE id_jadwal=?1 AND STATUS='AKTIF')a LEFT JOIN (SELECT COUNT(aa.id)AS mangkir,aa.id AS id_krs FROM presensi_mahasiswa AS aa INNER JOIN sesi_kuliah AS bb ON aa.id_sesi_kuliah=bb.id  INNER JOIN presensi_dosen AS cc ON bb.id_presensi_dosen=cc.id WHERE aa.status='AKTIF' AND aa.status_presensi IN ('MANGKIR','TERLAMBAT') GROUP BY aa.id)b  ON a.id= b.id_krs LEFT JOIN (SELECT * FROM enable_fiture WHERE ENABLE='1' AND fitur='UAS' AND id_tahun_akademik =?2)c ON a.id_mahasiswa = c.id_mahasiswa INNER JOIN mahasiswa AS d ON a.id_mahasiswa=d.id)aaa WHERE mangkir < 4 AND fitur='UAS' ORDER BY id_mahasiswa" , nativeQuery = true)
    List<Object[]> absenUas(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "SELECT id_mahasiswa as nim,nama,kode_uts as kode FROM (SELECT a.*,d.nama,COALESCE(b.mangkir,0) AS mangkir,c.fitur FROM (SELECT * FROM krs_detail WHERE id_jadwal=?1 AND STATUS='AKTIF')a LEFT JOIN (SELECT COUNT(aa.id)AS mangkir,aa.id AS id_krs FROM presensi_mahasiswa AS aa INNER JOIN sesi_kuliah AS bb ON aa.id_sesi_kuliah=bb.id  INNER JOIN presensi_dosen AS cc ON bb.id_presensi_dosen=cc.id WHERE aa.status='AKTIF' AND aa.status_presensi IN ('MANGKIR','TERLAMBAT') GROUP BY aa.id)b  ON a.id= b.id_krs LEFT JOIN (SELECT * FROM enable_fiture WHERE ENABLE='1' AND fitur='UTS' AND id_tahun_akademik =?2)c ON a.id_mahasiswa = c.id_mahasiswa INNER JOIN mahasiswa AS d ON a.id_mahasiswa=d.id)aaa WHERE mangkir < 4 AND fitur='UTS' ORDER BY id_mahasiswa" , nativeQuery = true)
    List<Object[]> absenUts(Jadwal jadwal, TahunAkademik tahunAkademik);

    @Query(value = "select aa.*,coalesce(bb.total_mahasiswa,0)as total_mahasiswa,coalesce(total_isi_edom,0) as total_isi_edom ,coalesce(round(e1,2),0)as e1,coalesce(round(e2,2),0) as e2,coalesce(round(e3,2),0)e3,coalesce(round(e4,2),0)e4,coalesce(round(e5,2),0)as e5, (coalesce(round(e1,2),0)+coalesce(round(e2,2),0)+coalesce(round(e3,2),0)+coalesce(round(e4,2),0)+coalesce(round(e5,2),0))/5 as total from (select a.id,c.nama_matakuliah, f.nama_karyawan, g.nama_kelas from jadwal as a left join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id left join matakuliah as c on b.id_matakuliah=c.id left join (select * from jadwal_dosen where status_jadwal_dosen ='PENGAMPU') as d on a.id=d.id_jadwal left join dosen as e on d.id_dosen = e.id left join karyawan as f on e.id_karyawan = f.id left join kelas as g on a.id_kelas=g.id where a.id_tahun_akademik=?1 and a.id_prodi = ?2 and a.status='AKTIF')aa left join (select count(a.id) as total_mahasiswa,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where b.id_tahun_akademik=?1 and a.status='AKTIF' and b.status='AKTIF' group by id_jadwal)bb on aa.id=bb.id_jadwal left join (select count(a.id) as total_isi_edom,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where b.id_tahun_akademik=?1 and a.status='AKTIF' and b.status='AKTIF' and a.status_edom='DONE' group by id_jadwal)cc on aa.id=cc.id_jadwal left join (select sum(e1)/count(a.id)as e1,sum(e2)/count(a.id)as e2,sum(e3)/count(a.id)as e3,sum(e4)/count(a.id)as e4,sum(e5)/count(a.id)as e5,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where b.id_tahun_akademik=?1 and a.status='AKTIF' and b.status='AKTIF' and a.status_edom='DONE' group by id_jadwal)dd on aa.id=dd.id_jadwal where nama_karyawan is not null order by nama_karyawan", nativeQuery = true)
    List<Object[]> rekapEdom(TahunAkademik tahunAkademik, Prodi prodi);

    @Query(value = "select aa.*,coalesce(bb.total_mahasiswa,0)as total_mahasiswa,coalesce(total_isi_edom,0) as total_isi_edom ,coalesce(round(e1,2),0)as e1,coalesce(round(e2,2),0) as e2,coalesce(round(e3,2),0)e3,coalesce(round(e4,2),0)e4,coalesce(round(e5,2),0)as e5, (coalesce(round(e1,2),0)+coalesce(round(e2,2),0)+coalesce(round(e3,2),0)+coalesce(round(e4,2),0)+coalesce(round(e5,2),0))/5 as total from (select a.id,c.nama_matakuliah, f.nama_karyawan, g.nama_kelas from jadwal as a left join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id left join matakuliah as c on b.id_matakuliah=c.id left join (select * from jadwal_dosen where status_jadwal_dosen ='PENGAMPU') as d on a.id=d.id_jadwal left join dosen as e on d.id_dosen = e.id left join karyawan as f on e.id_karyawan = f.id left join kelas as g on a.id_kelas=g.id where a.id =?1 and a.status='AKTIF')aa left join (select count(a.id) as total_mahasiswa,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where a.id_jadwal = ?1 and a.status='AKTIF' and b.status='AKTIF' group by id_jadwal)bb on aa.id=bb.id_jadwal left join (select count(a.id) as total_isi_edom,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where a.id_jadwal = ?1 and a.status='AKTIF' and b.status='AKTIF' and a.status_edom='DONE' group by id_jadwal)cc on aa.id=cc.id_jadwal left join (select sum(e1)/count(a.id)as e1,sum(e2)/count(a.id)as e2,sum(e3)/count(a.id)as e3,sum(e4)/count(a.id)as e4,sum(e5)/count(a.id)as e5,a.id_jadwal from krs_detail as a left join krs as b on a.id_krs = b.id where a.id_jadwal = ?1 and a.status='AKTIF' and b.status='AKTIF' and a.status_edom='DONE' group by id_jadwal)dd on aa.id=dd.id_jadwal where nama_karyawan is not null order by nama_karyawan", nativeQuery = true)
    Object edomJadwal(Jadwal jadwal);

    @Query(value = "select sum(jumlah_sks)as jml from krs_detail as a " +
            "inner join jadwal as b on a.id_jadwal = b.id \n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id " +
            "where a.id_mahasiswa=?1 and a.status='AKTIF' " +
            "and b.id_tahun_akademik=?2 " +
            "group by a.id_mahasiswa", nativeQuery = true)
    Long jumlahSksMahasiswa(String idMahasiswa, String idTahunAkademik);

    @Query(value = "SELECT COUNT(id_tahun_akademik)AS semester FROM\n" +
            "(SELECT a.* FROM krs_detail AS a \n" +
            "INNER JOIN krs AS b ON a.id_krs = b.id inner join tahun_akademik as c on b.id_tahun_akademik =c.id\n" +
            "WHERE a.id_mahasiswa = ?1 and b.id_tahun_akademik <> ?2 AND a.status = 'AKTIF' AND b.status = 'AKTIF' and c.jenis <> 'PENDEK'\n" +
            "GROUP BY a.id_tahun_akademik)aa", nativeQuery = true)
    Integer cariSemester(String idMahasiswa, String idTahunAKademik);

    @Query(value = "select count(id) as semester from krs where status = 'AKTIF' and id_mahasiswa = ?1 and id_tahun_akademik = ?2",nativeQuery = true)
    Integer cariSemesterSekarang (String idMaasiswa, String idTahunAKademik);


    @Query(value = "select a.id, c.nama_matakuliah, f.nama_karyawan, a.nilai_akhir from krs_detail as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah=c.id inner join jadwal as d on d.id=a.id_jadwal \n" +
            "inner join dosen as e on d.id_dosen_pengampu=e.id \n" +
            "inner join karyawan as f on e.id_karyawan=f.id where a.id_mahasiswa=?1 and a.status='AKTIF' and\n" +
            "a.status_konversi='AKTIF'", nativeQuery = true)
    List<Object[]> listKrsDetail(String idMahasiswa);

    KrsDetail findByJadwalAndTahunAkademikAndMahasiswaAndStatus(Jadwal jadwal, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, StatusRecord aktif);

    KrsDetail findByMahasiswaAndTahunAkademikAndJadwalAndStatus(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Jadwal jadwal, StatusRecord aktif);



    @Modifying
    @Query(value = "INSERT INTO krs_nilai_tugas (id,id_krs_detail,id_bobot_tugas,nilai,STATUS,nilai_akhir)\n" +
            "(SELECT UUID() AS id,aa.*,0 AS nilai,'AKTIF' AS STATUS,0 AS nilai_akhir FROM\n" +
            "(SELECT a.id AS krs_detail, c.id AS jadwal_bobot_tugas FROM krs_detail AS a\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN jadwal_bobot_tugas AS c ON b.id = c.id_jadwal\n" +
            "WHERE b.id= ?1 AND a.status='AKTIF')aa\n" +
            "LEFT JOIN \n" +
            "(SELECT * FROM krs_nilai_tugas WHERE STATUS='AKTIF') bb ON aa.krs_detail = bb.id_krs_detail AND aa.jadwal_bobot_tugas = bb.id_bobot_tugas\n" +
            "WHERE bb.id IS NULL)", nativeQuery = true)
    int insertNilaiTugas(String idJadwal);

    @Query(value = "\n" +
            "SELECT UUID() AS id,aa.krs_detail AS krsDetail,jadwal_bobot_tugas AS jadwalBobotTugas,0 AS nilai,'AKTIF' AS STATUS,0 AS nilaiAkhir FROM\n" +
            "(SELECT a.id AS krs_detail, c.id AS jadwal_bobot_tugas FROM krs_detail AS a\n" +
            "INNER JOIN jadwal AS b ON a.id_jadwal = b.id\n" +
            "INNER JOIN jadwal_bobot_tugas AS c ON b.id = c.id_jadwal\n" +
            "WHERE b.id=?1 AND a.status='AKTIF')aa\n" +
            "LEFT JOIN \n" +
            "(SELECT * FROM krs_nilai_tugas WHERE STATUS='AKTIF') bb ON aa.krs_detail = bb.id_krs_detail AND aa.jadwal_bobot_tugas = bb.id_bobot_tugas\n" +
            "WHERE bb.id IS NULL", nativeQuery = true)
    List<KrsNilaiTugasDto> listKrsNilaiTugas(String idJadwal);


    @Query(value="select aa.*,bb.id, bb.kode_tahun_akademik,bb.nama_tahun_akademik,jenis from\n" +
            "(select id_tahun_akademik,kode_matakuliah,nama_matakuliah,nama_matakuliah_english,jumlah_sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots order by kode_tahun_akademik)aa\n" +
            "inner join tahun_akademik as bb on aa.id_tahun_akademik = bb.id \n" +
            "order by kode_tahun_akademik\n", nativeQuery = true)
    List<Object[]> transkriptPrint1(String idMahasiswa);

    @Query(value="select id_tahun_akademik,kode_matakuliah,nama_matakuliah,nama_matakuliah_english,jumlah_sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where e.semester = ?2 and a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots\n" +
            "order by kode_matakuliah", nativeQuery = true)
    List<Object[]> transkriptAkhir(String idMahasiswa, String semester);

    @Query(value="select bb.id, bb.kode_tahun_akademik,bb.nama_tahun_akademik,jenis,count(bb.id) as rowspan from\n" +
            "(select id_tahun_akademik,kode_matakuliah,nama_matakuliah,nama_matakuliah_english,jumlah_sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots order by kode_tahun_akademik)aa\n" +
            "inner join tahun_akademik as bb on aa.id_tahun_akademik = bb.id \n" +
            "group by bb.id order by kode_tahun_akademik", nativeQuery = true)
    List<Object[]> semesterTraskripPrint1(String idMahasiswa);



    @Query(value = "select bbb.*,aaa.* from\n" +
            "(select bb.id, bb.kode_tahun_akademik,bb.nama_tahun_akademik,jenis,count(bb.id) as rowspan from\n" +
            "(select id_tahun_akademik,kode_matakuliah,nama_matakuliah,nama_matakuliah_english,jumlah_sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots order by kode_tahun_akademik)aa\n" +
            "inner join tahun_akademik as bb on aa.id_tahun_akademik = bb.id \n" +
            "group by bb.id order by kode_tahun_akademik)aaa\n" +
            "left join\n" +
            "(select id_tahun_akademik,kode_matakuliah,nama_matakuliah,nama_matakuliah_english,jumlah_sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots order by kode_tahun_akademik) bbb on aaa.id = bbb.id_tahun_akademik", nativeQuery = true)
    List<Object[]> transkriptAkhir(String idMahasiswa);


    @Query(value = "select c.nama_tahun_akademik, sum(e.jumlah_sks), ROUND(SUM(COALESCE(a.bobot,0)*e.jumlah_sks)/SUM(e.jumlah_sks),2)AS ipk from krs_detail as a " +
            "inner join krs as b on b.id = a.id_krs inner join tahun_akademik as c on c.id = b.id_tahun_akademik inner join jadwal as d on d.id = a.id_jadwal " +
            "inner join matakuliah_kurikulum as e on e.id = d.id_matakuliah_kurikulum where b.id_mahasiswa =?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 " +
            "group by c.nama_tahun_akademik", nativeQuery = true)
    List<Object[]> historyMahasiswa(Mahasiswa mahasiswa);

    @Query(value = "select b.id, b.nama_tahun_akademik,b.jenis from krs as a inner join krs_detail as g on a.id = g.id_krs inner join jadwal as h on g.id_jadwal = h.id inner join matakuliah_kurikulum as i on h.id_matakuliah_kurikulum = i.id inner join tahun_akademik as b on a.id_tahun_akademik = b.id where a.status = 'AKTIF' and g.status='AKTIF' and i.jumlah_sks > 0 and a.id_mahasiswa = ?1 group by a.id order by b.id desc", nativeQuery = true)
    List<Object[]> semesterHistory(Mahasiswa mahasiswa);

    @Query(value = "select kd.* from krs_detail as kd inner join matakuliah_kurikulum as mk on kd.id_matakuliah_kurikulum = mk.id where kd.id_mahasiswa = ?1 and kd.status = 'AKTIF' and mk.konsep_note = 'METOLIT' and kd.nilai_akhir >= 55 limit 1", nativeQuery = true)
    Object[] validasiMetolit(Mahasiswa mahasiswa);

    @Query(value = "select kd.* from krs_detail as kd inner join matakuliah_kurikulum as mk on kd.id_matakuliah_kurikulum = mk.id where kd.id_mahasiswa = ?1 and kd.status = 'AKTIF' and mk.konsep_note = 'MAGANG' and kd.nilai_akhir >= 55 limit 1", nativeQuery = true)
    Object[] validasiMagang(Mahasiswa mahasiswa);

    @Query(value = "select d.id as kodeakademik ,b.id,mk.kode_matakuliah as kode ,mk.nama_matakuliah_english as matakuliah,b.nilai_presensi as presensi ,b.nilai_tugas as tugas,b.nilai_uts as uts,b.nilai_uas as uas, coalesce (b.nilai_akhir,0) as nilaiAkhir,coalesce(c.bobot,0)  as bobot,coalesce (c.nama,'E') as grade from krs as a inner join krs_detail as b on a.id = b.id_krs left join grade as c on b.nilai_akhir >= c.bawah and b.nilai_akhir <= c.atas inner join matakuliah_kurikulum as m on b.id_matakuliah_kurikulum = m.id inner join matakuliah as mk on m.id_matakuliah = mk.id inner join tahun_akademik as d on d.id = a.id_tahun_akademik where a.id_mahasiswa= ?1 and a.status='AKTIF' and b.status='aktif' order by d.id desc", nativeQuery = true)
    List<Object[]> khsHistoty(Mahasiswa mahasiswa);

    @Query(value = "SELECT ROUND(SUM(mutu)/SUM(jumlah_sks),2) AS ipk FROM (SELECT nilai_akhir,f.bobot,f.nama AS grade,jumlah_sks, f.bobot * jumlah_sks AS mutu FROM krs_detail AS a INNER JOIN krs AS b ON a.id_krs = b.id INNER JOIN tahun_akademik AS c ON b.id_tahun_akademik = c.id INNER JOIN jadwal AS d ON a.id_jadwal = d.id INNER JOIN matakuliah_kurikulum AS e ON d.id_matakuliah_kurikulum = e.id INNER JOIN grade AS f ON a.nilai_akhir <= f.atas AND a.nilai_akhir >= f.bawah WHERE a.id_mahasiswa = ?1 AND a.status = 'AKTIF' AND b.status='AKTIF' AND c.kode_tahun_akademik <= ?2 AND e.jumlah_sks > 0 AND nilai_akhir IS NOT NULL)ss", nativeQuery = true)
    IpkDto ipkTahunAkademik(Mahasiswa mahasiswa,String tahunAkademik);

    @Query(value="select id_tahun_akademik as tahun,kode_matakuliah as kode,nama_matakuliah as matakuliah,nama_matakuliah_english as courses,jumlah_sks as sks,bobot,grade,mutu from\n" +
            "(select aa.*,coalesce(bb.bobot,0.00)as bobots from\n" +
            "(select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a \n" +
            "inner join krs as b on a.id_krs = b.id\n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "inner join jadwal as d on a.id_jadwal = d.id\n" +
            "inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id\n" +
            "inner join matakuliah as f on e.id_matakuliah = f.id\n" +
            "where e.semester = ?2 and a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa\n" +
            "left join\n" +
            "(select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
            "inner join matakuliah as d on c.id_matakuliah = d.id\n" +
            "where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb\n" +
            "on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa \n" +
            "where bobot > bobots\n" +
            "order by kode_matakuliah", nativeQuery = true)
    List<TranskriptDto> excelTranskript(String idMahasiswa, String semester);

    @Query(value = "select  id from krs_detail where id_jadwal = ?1 and id_mahasiswa = ?2 and status = 'AKTIF' order by nilai_akhir desc Limit 1", nativeQuery = true)
    Object getKrsDetailId(Jadwal jadwal,Mahasiswa mahasiswa);
    @Query(value = "select  id from krs_detail where id_jadwal = ?1 and id_mahasiswa = ?2 and status = 'AKTIF' and id_krs =?3 and id_tahun_akademik = ?4", nativeQuery = true)
    Object getKrsDetailId2(Jadwal jadwal,Mahasiswa mahasiswa, Krs krs, TahunAkademik tahunAkademik);

    @Modifying
    @Query(value = "update krs_detail set status = 'HAPUS' where id_jadwal = ?1 and id_mahasiswa = ?2 and status = 'AKTIF' and id not in(?3)", nativeQuery = true)
    void updateKrsDetail(Jadwal jadwal, Mahasiswa mahasiswa, String id);

    List<KrsDetail> findByStatusAndJadwalAndMahasiswaAndIdNotIn(StatusRecord statusRecord, Jadwal jadwal, Mahasiswa mahasiswa, Object Id);

    Long countByJadwalIdAndKrsAndStatusAndTahunAkademik(String jadwal, Krs krs , StatusRecord statusRecord, TahunAkademik tahunAkademik);

    KrsDetail findByMahasiswaAndJadwalAndStatusAndKrsAndTahunAkademik(Mahasiswa byNim, Jadwal jadwal, StatusRecord aktif, Krs krs,TahunAkademik tahunAkademik);

    List<KrsDetail> findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa,TahunAkademik tahunAkademik);

    @Query(value = "select distinct bobot from (select aa.*,coalesce(bb.bobot,0.00)as bobots from (select b.id_Tahun_akademik,a.id_krs,f.kode_matakuliah,f.nama_matakuliah,f.nama_matakuliah_english,e.jumlah_sks,bobot,grade,bobot*jumlah_sks as mutu, c.kode_tahun_akademik from krs_detail as a inner join krs as b on a.id_krs = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id inner join jadwal as d on a.id_jadwal = d.id inner join matakuliah_kurikulum as e on d.id_matakuliah_kurikulum = e.id inner join matakuliah as f on e.id_matakuliah = f.id where a.id_mahasiswa=?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and e.jumlah_sks > 0 and grade <> 'E')aa left join (select a.id_krs,d.kode_matakuliah,d.nama_matakuliah,d.nama_matakuliah_english,bobot from krs_detail as a inner join jadwal as b on a.id_jadwal = b.id inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id inner join matakuliah as d on c.id_matakuliah = d.id where a.id_mahasiswa=?1 and a.status = 'AKTIF' and c.jumlah_sks > 0 and grade <> 'E')bb on aa.kode_matakuliah = bb.kode_matakuliah and aa.id_krs <> bb.id_krs)aa where bobot > bobots and bobot < 3.00 order by kode_matakuliah limit 1", nativeQuery = true)
    BigDecimal validasiTranskrip(Mahasiswa mahasiswa);

    List<KrsDetail> findByMahasiswaAndTahunAkademikAndStatusAndKrsNull(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, StatusRecord status);

    List<KrsDetail> findByStatusAndJadwalOrderByMahasiswaNim(StatusRecord status, Jadwal jadwal);

    KrsDetail findByStatusAndMahasiswaAndJadwal(StatusRecord statusRecord, Mahasiswa mahasiswa, Jadwal jadwal);

    KrsDetail findByStatusAndMahasiswaNimAndJadwal(StatusRecord statusRecord, String mahasiswa, Jadwal jadwal);

    KrsDetail findByTahunAkademikAndJadwalProdiAndJadwalAndStatus(TahunAkademik tahunAkademik, Prodi prodi, Jadwal jadwal,StatusRecord statusRecord);

    KrsDetail findByTahunAkademikAndJadwalAndMahasiswaAndKrsAndStatus(TahunAkademik tahunAkademik, Jadwal jadwal,Mahasiswa mahasiswa,Krs krs,StatusRecord statusRecord);

    @Query(value = "SELECT kd.id,id_krs,id_mahasiswa,id_jadwal,kd.id_matakuliah_kurikulum,nilai_presensi,nilai_tugas,nilai_uts,nilai_uas,kd.status as STATUS,nilai_akhir,bobot,grade,kd.id_tahun_akademik,ta.nama_tahun_akademik,kd.created_by,kd.last_modified_by,kd.last_modified_time, kd.status_edom \n" +
            "FROM krs_detail as kd \n" +
            "inner join jadwal as jdl on kd.id_jadwal = jdl.id\n" +
            "inner join tahun_akademik as ta on kd.id_tahun_akademik = ta.id\n" +
            "where kd.id_tahun_akademik = ?1 and jdl.id_prodi = ?2 and id_jadwal = ?1 and kd.status = 'AKTIF' ", nativeQuery = true)
    Object getKrsDetailId3(TahunAkademik tahunAkademik, Prodi prodi, Jadwal jadwal, StatusRecord statusRecord);

    KrsDetail findByTahunAkademikAndJadwalProdiAndJadwalAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Prodi prodi, Jadwal jadwal, Mahasiswa mahasiswa,StatusRecord statusRecord);

    @Query(value = "select kd.id,kd.id_krs,kd.id_mahasiswa,kd.id_jadwal,kd.id_matakuliah_kurikulum,kd.nilai_presensi,kd.nilai_tugas,kd.nilai_uts,kd.nilai_uas,\n" +
            "kd.finalisasi,kd.status,kd.nilai_akhir,kd.bobot,kd.grade,kd.jumlah_kehadiran,kd.jumlah_terlambat,kd.jumlah_mangkir,kd.jumlah_izin,\n" +
            "kd.jumlah_sakit,kd.created_by,kd.last_modified_by,kd.created_time,kd.last_modified_time,kd.kode_uts,kd.kode_uas,kd.e1,kd.e2,kd.e3,kd.e4,kd.e5,\n" +
            "kd.status_edom,kd.id_tahun_akademik,kd.nilai_uts_final,kd.nilai_uas_final,kd.ket,kd.status_konversi,kd.nilai_sds,kd.nilai_lama,jd.id_number_elearning\n" +
            "from krs_detail as kd\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "where jd.id_number_elearning = ?1 and kd.id_mahasiswa = ?2\n" +
            "and kd.id_krs = ?3 and kd.id_tahun_akademik = ?4 and kd.status = 'AKTIF'\n", nativeQuery = true)
    KrsDetail getKrsDetail4(String jadwal, Mahasiswa mahasiswa, Krs krs,TahunAkademik tahunAkademik, StatusRecord statusRecord);

    @Query(value = "select count(*) from krs_detail as kd\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "where jd.id_number_elearning = ?1 and kd.id_mahasiswa = ?2\n" +
            "and kd.id_tahun_akademik = ?3 and kd.status = 'AKTIF'", nativeQuery = true)
    Long countKrsDetail2(String jadwal, Mahasiswa mahasiswa, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    List<KrsDetail> findByStatusAndJadwalId(StatusRecord statusRecord, String id);


    @Query(value = "update krs_detail set nilai_akhir = (coalesce(nilai_tugas,0) + coalesce(nilai_uts_final,0) + coalesce(nilai_uas_final,0)) - (10.00 - ?1), nilai_sds = ?1 where id = ?2", nativeQuery = true)
    Update updateNilaiSds(BigDecimal nilaiSDS, String id);

    @Query(value = "UPDATE krs_detail k, grade p, jadwal j\n" +
            "SET k.grade = p.nama, k.bobot = p.bobot\n" +
            "WHERE k.id_jadwal = j.id and k.nilai_akhir <= p.atas \n" +
            "AND k.nilai_akhir >= p.bawah AND k.status = 'AKTIF' AND k.id = ?1", nativeQuery = true)
    Update updateGradeNilai(String id);

    @Query(value = "select semester,id_matakuliah_kurikulum,kode_matakuliah as kode, nama_matakuliah as matkul, nama_matakuliah_english as course , jumlah_sks as sks, max(nilai_akhir) as nilai_akhir,b.bobot,b.nama as grade,b.bobot*jumlah_sks as mutu from (select semester,id_matakuliah_kurikulum,kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,max(nilai_akhir) as nilai_akhir, max(bobot)as bobot, min(grade)as grade from (select d.semester,c.id_matakuliah_kurikulum,e.kode_matakuliah, e.nama_matakuliah, e.nama_matakuliah_english, d.jumlah_sks, max(a.nilai_akhir) as nilai_akhir, max(a.bobot)as bobot, min(a.grade)as grade from krs_detail as a inner join krs as b on a.id_krs = b.id inner join jadwal as c on a.id_jadwal = c.id inner join matakuliah_kurikulum as d on c.id_matakuliah_kurikulum = d.id inner join matakuliah as e on d.id_matakuliah = e.id where a.status = 'AKTIF' and a.nilai_akhir is not null and a.id_mahasiswa = ?1 and d.jumlah_sks > 0 group by e.kode_matakuliah)a group by nama_matakuliah)a inner join grade as b on coalesce(a.nilai_akhir,0) <= b.atas and coalesce(a.nilai_akhir,0) >= b.bawah group by nama_matakuliah_english order by semester, kode_matakuliah",nativeQuery = true)
    List<DataTranskript> listTranskript(Mahasiswa mahasiswa);

    @Query(value = "select semester,id_matakuliah_kurikulum,kode_matakuliah as kode, nama_matakuliah as matkul, nama_matakuliah_english as course , jumlah_sks as sks, max(nilai_akhir) as nilai_akhir,b.bobot,b.nama as grade,b.bobot*jumlah_sks as mutu from (select semester,id_matakuliah_kurikulum,kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,max(nilai_akhir) as nilai_akhir, max(bobot)as bobot, min(grade)as grade from (select d.semester,c.id_matakuliah_kurikulum,e.kode_matakuliah, e.nama_matakuliah, e.nama_matakuliah_english, d.jumlah_sks, max(a.nilai_akhir) as nilai_akhir, max(a.bobot)as bobot, min(a.grade)as grade from krs_detail as a inner join krs as b on a.id_krs = b.id inner join jadwal as c on a.id_jadwal = c.id inner join matakuliah_kurikulum as d on c.id_matakuliah_kurikulum = d.id inner join matakuliah as e on d.id_matakuliah = e.id where a.status = 'AKTIF' and a.nilai_akhir is not null and d.semester = ?2 and a.id_mahasiswa = ?1 and d.jumlah_sks > 0 group by e.kode_matakuliah)a group by nama_matakuliah)a inner join grade as b on coalesce(a.nilai_akhir,0) <= b.atas and coalesce(a.nilai_akhir,0) >= b.bawah group by nama_matakuliah_english order by semester, kode_matakuliah",nativeQuery = true)
    List<DataTranskript> listTranskriptSemester(Mahasiswa mahasiswa, Integer semester);

}
