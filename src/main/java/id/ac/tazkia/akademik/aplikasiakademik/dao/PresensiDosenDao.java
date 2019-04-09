package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.PresensiDosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen, String> {
    Page<PresensiDosen> findByStatusNotInAndJadwal(StatusRecord status, Jadwal jadwal, Pageable pageable);
}
