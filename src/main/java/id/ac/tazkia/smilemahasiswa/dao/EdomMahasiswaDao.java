package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.boot.autoconfigure.quartz.QuartzDataSource;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EdomMahasiswaDao extends PagingAndSortingRepository<EdomMahasiswa, String> {

    List<EdomMahasiswa> findByStatusOrderByEdomQuestionNomor(StatusRecord statusRecord);

    @Query(value = "select a.id_jadwal, id_dosen, nama_matakuliah, nama_kelas, dosen, jumlah_mahasiswa, responden, round(sum(average)/count(id_edom_question),2) as rata2 from\n" +
            "(select a.id_jadwal, g.nama_matakuliah, h.nama_kelas,a.id_dosen, e.nama_karyawan as dosen, id_krs_detail,a.id_edom_question, c.pertanyaan, sum(a.nilai)as nilai, count(a.id_jadwal) as responden,sum(a.nilai)/count(a.id_jadwal)as average\n" +
            "from edom_mahasiswa as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join edom_question as c on a.id_edom_question = c.id\n" +
            "inner join dosen as d on a.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "inner join matakuliah_kurikulum as f on b.id_matakuliah_kurikulum = f.id\n" +
            "inner join matakuliah as g on f.id_matakuliah = g.id\n" +
            "inner join kelas as h on b.id_kelas = h.id\n" +
            "where b.id_tahun_akademik = ?1 and b.id_prodi = ?2 and b.status = 'AKTIF'\n" +
            "group by a.id_jadwal, a.id_dosen,id_edom_question\n" +
            "order by id_jadwal,id_dosen,c.nomor)a\n" +
            "inner join\n" +
            "(select count(b.id)as jumlah_mahasiswa, id_jadwal from krs_detail as a inner join jadwal as b on a.id_jadwal = b.id where b.id_tahun_akademik = ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' and b.id_prodi = ?2  group by id_jadwal)b on a.id_jadwal = b.id_jadwal\n" +
            "group by id_jadwal, id_dosen", nativeQuery = true)
    List<Object[]> headerEdomMahasiswa(TahunAkademik tahunAkademik, Prodi prodi);

    @Query(value = "select a.id_jadwal, id_dosen, nama_matakuliah, nama_kelas, dosen, jumlah_mahasiswa, responden, round(sum(average)/count(id_edom_question),2) as rata2 from\n" +
            "(select a.id_jadwal, g.nama_matakuliah, h.nama_kelas,a.id_dosen, e.nama_karyawan as dosen, id_krs_detail,a.id_edom_question, c.pertanyaan, sum(a.nilai)as nilai, \n" +
            "count(a.id_jadwal) as responden,sum(a.nilai)/count(a.id_jadwal)as average\n" +
            "from edom_mahasiswa as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join edom_question as c on a.id_edom_question = c.id\n" +
            "inner join dosen as d on a.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "inner join matakuliah_kurikulum as f on b.id_matakuliah_kurikulum = f.id\n" +
            "inner join matakuliah as g on f.id_matakuliah = g.id\n" +
            "inner join kelas as h on b.id_kelas = h.id\n" +
            "where b.id=?1 and d.id=?2 and b.status = 'AKTIF'\n" +
            "group by a.id_jadwal, a.id_dosen,id_edom_question\n" +
            "order by id_jadwal,id_dosen,c.nomor)a\n" +
            "inner join\n" +
            "(select count(b.id)as jumlah_mahasiswa, id_jadwal from krs_detail as a inner join jadwal as b on a.id_jadwal = b.id where b.id = ?1 and a.status = 'AKTIF' " +
            "and b.status = 'AKTIF' and b.id_dosen_pengampu = ?2  group by id_jadwal)b on a.id_jadwal = b.id_jadwal\n" +
            "group by id_jadwal, id_dosen", nativeQuery = true)
    List<Object[]> headerEdomMahasiswaPerDosen(Jadwal jadwal, Dosen dosen);

    @Query(value = "select a.id_jadwal, coalesce(nidn,'-') as nidn, dosen, coalesce(status_dosen,'-') as status_dosen, nama_prodi, email, nama_matakuliah, semester, nama_kelas, jumlah_mahasiswa, responden, round(sum(average)/count(id_edom_question),2) as nilai_edom from\n" +
            "(select a.id_jadwal, g.nama_matakuliah, h.nama_kelas, i.nama_prodi, a.id_dosen, e.nama_karyawan as dosen, d.status_dosen, e.nidn, e.email, f.semester, id_krs_detail,a.id_edom_question, c.pertanyaan, sum(a.nilai)as nilai, \n" +
            "count(a.id_jadwal) as responden,sum(a.nilai)/count(a.id_jadwal)as average\n" +
            "from edom_mahasiswa as a\n" +
            "inner join jadwal as b on a.id_jadwal = b.id\n" +
            "inner join edom_question as c on a.id_edom_question = c.id\n" +
            "inner join dosen as d on a.id_dosen = d.id\n" +
            "inner join karyawan as e on d.id_karyawan = e.id\n" +
            "inner join matakuliah_kurikulum as f on b.id_matakuliah_kurikulum = f.id\n" +
            "inner join matakuliah as g on f.id_matakuliah = g.id\n" +
            "inner join kelas as h on b.id_kelas = h.id\n" +
            "inner join prodi as i on b.id_prodi = i.id\n" +
            "where b.id_tahun_akademik = ?1 and b.status = 'AKTIF'\n" +
            "group by a.id_jadwal, a.id_dosen,id_edom_question\n" +
            "order by id_jadwal,id_dosen,c.nomor)a\n" +
            "inner join\n" +
            "(select count(b.id)as jumlah_mahasiswa, id_jadwal from krs_detail as a inner join jadwal as b on a.id_jadwal = b.id where b.id_tahun_akademik = ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' group by id_jadwal)b on a.id_jadwal = b.id_jadwal\n" +
            "group by id_jadwal, id_dosen order by nama_prodi", nativeQuery = true)
    List<Object[]> downloadEdom(TahunAkademik tahunAkademik);

}
