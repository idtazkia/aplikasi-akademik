package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Krs;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahMbkm;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranMbkm;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatakuliahMbkmDao extends PagingAndSortingRepository<MatakuliahMbkm, String> {

    @Query("select sum (mm.matakuliahKurikulum.jumlahSks) from MatakuliahMbkm mm where mm.status = :status and mm.krs= :krs")
    Long jumlahSksMbkm (@Param("status") StatusRecord statusRecord, @Param("krs") Krs krs);

    List<MatakuliahMbkm> findByPendaftaranMbkmAndStatus(PendaftaranMbkm mbkm, StatusRecord statusRecord);

    List<MatakuliahMbkm> findByKrsAndStatus(Krs krs, StatusRecord statusRecord);

}
