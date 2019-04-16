package id.ac.tazkia.akademik.aplikasiakademik.dto;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface NilaiTugasDao extends PagingAndSortingRepository <NilaiTugas,String> {
    NilaiTugas findByStatusAndBobotTugasAndKrsDetail(StatusRecord status, BobotTugas bobotTugas, KrsDetail krsDetail);
    List<NilaiTugas> findByStatusAndKrsDetail(StatusRecord status, KrsDetail krsDetail);
    List<NilaiTugas> findByStatusAndKrsDetailJadwal(StatusRecord status, Jadwal jadwal);
}
