package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JadwalTugas;
import id.ac.tazkia.smilemahasiswa.entity.JadwalTugasJawaban;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JadwalTugasJawabanDao extends PagingAndSortingRepository<JadwalTugasJawaban, String> {

    List<JadwalTugasJawaban> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord status);

    List<JadwalTugasJawaban> findByJadwalTugasAndStatusOrderByWaktuPengumpulan(JadwalTugas jadwalTugas, StatusRecord status);

    List<JadwalTugasJawaban> findByJadwalTugasAndMahasiswa(JadwalTugas jadwalTugas, Mahasiswa mahsiswa);

    @Query(value = "select coalesce(jt.maks_file_upload - count(id_jadwal_tugas), 0) as totalUpload from jadwal_tugas_jawaban tj right join jadwal_tugas jt on jt.id=tj.id_jadwal_tugas where id_mahasiswa=?1 and jt.id=?2 group by id_jadwal_tugas;", nativeQuery = true)
    Integer cariTotalTugas(String idMahasiswa, String idJadwalTugas);

    JadwalTugasJawaban findTopByMahasiswaAndJadwalTugasAndStatus(Mahasiswa mhs, JadwalTugas jadwalTugas, StatusRecord status);

}
