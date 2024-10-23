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

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'TUGAS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobot(@Param("jadwal")Jadwal jadwal);

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'UTS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobotUTS(@Param("jadwal")Jadwal jadwal);

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'UAS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobotUAS(@Param("jadwal")Jadwal jadwal);


    List<JadwalTugas> findByStatusAndSesiKuliahJadwalOrderBySesiKuliahPertemuanKe(StatusRecord status, Jadwal jadwal);

}

