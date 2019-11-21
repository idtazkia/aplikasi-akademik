package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.BobotDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.NilaiDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.BobotTugas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BobotTugasDao extends PagingAndSortingRepository<BobotTugas,String> {
    List<BobotTugas> findByJadwalAndStatus(Jadwal jadwal, StatusRecord status);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.NilaiDto(bt.id,bt.bobot) from BobotTugas bt where bt.jadwal.id= :jadwal and bt.status = :status")
    List<NilaiDto> bobotTugas(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.BobotDto(bt.id,bt.namaTugas,bt.bobot) from BobotTugas bt where bt.jadwal.id= :jadwal and bt.status = :status")
    List<BobotDto> Tugas(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select sum (bt.bobot) from BobotTugas bt where bt.jadwal.id = :id and bt.status = :status")
    Long sumBobot(@Param("id")String id,@Param("status")StatusRecord statusRecord);
}
