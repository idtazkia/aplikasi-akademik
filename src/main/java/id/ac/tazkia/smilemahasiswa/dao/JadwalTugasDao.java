package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JadwalTugas;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JadwalTugasDao extends PagingAndSortingRepository<JadwalTugas, String> {

    List<JadwalTugas> findBySesiKuliahJadwalAndStatusOrderByPertemuanKe(Jadwal jadwal, StatusRecord status);

    @Query("select sum(jt.bobot) from JadwalTugas jt where jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobot(@Param("jadwal")Jadwal jadwal);

}
