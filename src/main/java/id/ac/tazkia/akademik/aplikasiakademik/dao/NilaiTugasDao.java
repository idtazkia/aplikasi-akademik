package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.BobotDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NilaiTugasDao extends PagingAndSortingRepository <NilaiTugas,String> {
    NilaiTugas findByStatusAndBobotTugasAndKrsDetail(StatusRecord status, BobotTugas bobotTugas, KrsDetail krsDetail);
    List<NilaiTugas> findByStatusAndKrsDetail(StatusRecord status, KrsDetail krsDetail);
    List<NilaiTugas> findByStatusAndKrsDetailJadwal(StatusRecord status, Jadwal jadwal);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.BobotDto(nt.krsDetail.id,nt.bobotTugas.id,nt.nilai) from NilaiTugas nt where nt.status = :status and nt.krsDetail.id = :id")
    List<BobotDto> nilaiTugasList(@Param("status")StatusRecord statusRecord,@Param("id")String id);
}
