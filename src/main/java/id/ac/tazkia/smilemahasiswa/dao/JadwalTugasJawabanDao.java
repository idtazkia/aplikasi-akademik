package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JadwalTugas;
import id.ac.tazkia.smilemahasiswa.entity.JadwalTugasJawaban;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JadwalTugasJawabanDao extends PagingAndSortingRepository<JadwalTugasJawaban, String> {

    List<JadwalTugasJawaban> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord status);

    List<JadwalTugasJawaban> findByJadwalTugasAndStatusOrderByWaktuPengumpulan(JadwalTugas jadwalTugas, StatusRecord status);

    List<JadwalTugasJawaban> findByJadwalTugasAndMahasiswa(JadwalTugas jadwalTugas, Mahasiswa mahsiswa);

//    @Query("select coalesce(count(tj.id), 0) as total from JadwalTugasJawaban tj where tj.jadwalTugas = :jadwalTugas and tj.mahasiswa = :mahasiswa")
//    Object[] cariTotalTugas(@Param("jadwalTugas") JadwalTugas jadwalTugas, @Param("mahasiswa") Mahasiswa mahasiswa);

}
