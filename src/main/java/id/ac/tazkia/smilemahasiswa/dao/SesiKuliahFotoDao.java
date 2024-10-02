package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.SesiKuliahFoto;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SesiKuliahFotoDao extends PagingAndSortingRepository<SesiKuliahFoto, String>, CrudRepository<SesiKuliahFoto, String> {

    List<SesiKuliahFoto> findByStatusAndJadwalOrderBySesiKuliahPertemuan(StatusRecord statusRecord, Jadwal jadwal);


}
