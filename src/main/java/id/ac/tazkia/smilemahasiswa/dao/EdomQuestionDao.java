package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EdomQuestionDao extends PagingAndSortingRepository<EdomQuestion, String> {

    List<EdomQuestion> findByStatusOrderByNomor(StatusRecord statusRecord);
    EdomQuestion findByStatusAndNomorAndTahunAkademik(StatusRecord status,Integer nomor, TahunAkademik tahunAkademik );
    List<EdomQuestion> findByStatusAndTahunAkademikOrderByNomorAsc(StatusRecord status,TahunAkademik tahunAkademik);

    @Query(value = "select a.id_jadwal, id_dosen, nama_matakuliah, nama_kelas, dosen, jumlah_mahasiswa, responden, nomor ,pertanyaan, round(average,2) as nilai from\n" +
            "(select a.id_jadwal, g.nama_matakuliah, h.nama_kelas,a.id_dosen, e.nama_karyawan as dosen,c.nomor, id_krs_detail,a.id_edom_question, c.pertanyaan, sum(a.nilai)as nilai, count(a.id_jadwal) as responden,sum(a.nilai)/count(a.id_jadwal)as average\n" +
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
            "and b.status = 'AKTIF' and b.id_prodi = ?2  group by id_jadwal)b on a.id_jadwal = b.id_jadwal", nativeQuery = true)
    List<Object[]> detailEdom(TahunAkademik tahunAkademik, Prodi prodi);

    @Query(value = "select a.id_jadwal, id_dosen, nama_matakuliah, nama_kelas, dosen, jumlah_mahasiswa, responden, nomor ,pertanyaan, round(average,2) as nilai from\n" +
            "(select a.id_jadwal, g.nama_matakuliah, h.nama_kelas,a.id_dosen, e.nama_karyawan as dosen,c.nomor, id_krs_detail,a.id_edom_question, c.pertanyaan, sum(a.nilai)as nilai, \n" +
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
            "and b.status = 'AKTIF' and b.id_dosen_pengampu = ?2 group by id_jadwal)b on a.id_jadwal = b.id_jadwal", nativeQuery = true)
    List<Object[]> detailEdomPerDosen(Jadwal jadwal, Dosen dosen);

}
