package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Rps;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RpsDao extends PagingAndSortingRepository<Rps, String> {

    Rps findByJadwalAndStatus(Jadwal jadwal, StatusRecord aktif);
    Iterable<Rps> findByStatusAndJadwal(StatusRecord aktif, Jadwal jadwal);


}
