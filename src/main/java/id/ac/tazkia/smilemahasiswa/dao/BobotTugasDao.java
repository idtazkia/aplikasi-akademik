package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotScoreDto;
import id.ac.tazkia.smilemahasiswa.entity.BobotTugas;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BobotTugasDao extends PagingAndSortingRepository<BobotTugas, String> {
    List<BobotTugas> findByJadwalAndStatus(Jadwal jadwal, StatusRecord aktif);
    List<BobotTugas> findByJadwalAndStatusOrderByPertemuanAsc(Jadwal jadwal, StatusRecord aktif);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.assesment.BobotScoreDto(bt.id,bt.bobot) from BobotTugas bt where bt.jadwal.id= :jadwal and bt.status = :status")
    List<BobotScoreDto> bobotTugas(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto(bt.id,bt.pertemuan,bt.bobot) from BobotTugas bt where bt.jadwal.id= :jadwal and bt.status = :status order by bt.pertemuan asc")
    List<BobotDto> Tugas(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);
}
