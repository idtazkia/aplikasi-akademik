package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.BobotTugas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BobotTugasDao extends PagingAndSortingRepository<BobotTugas,String> {
    List<BobotTugas> findByJadwalAndStatus(Jadwal jadwal, StatusRecord status);
}
