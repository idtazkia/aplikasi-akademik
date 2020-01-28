package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NilaiTugasDao extends PagingAndSortingRepository<NilaiTugas, String> {
    @Query("select new id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto(nt.krsDetail.id,nt.bobotTugas.id,nt.nilai) from NilaiTugas nt where nt.status = 'AKTIF' and nt.krsDetail.jadwal = :jadwal and nt.bobotTugas.status = 'AKTIF'")
    List<BobotDto> nilaiTugasList(@Param("jadwal")Jadwal jadwal);

    NilaiTugas findByStatusAndBobotTugasAndKrsDetail(StatusRecord aktif, BobotTugas bobotTugas, KrsDetail krsDetail);

    List<NilaiTugas> findByStatusAndKrsDetailAndBobotTugasStatus(StatusRecord aktif, KrsDetail krsDetail, StatusRecord aktif1);
}
