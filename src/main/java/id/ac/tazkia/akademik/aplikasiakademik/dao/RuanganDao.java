package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ruangan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RuanganDao extends PagingAndSortingRepository<Ruangan, String> {
    Page<Ruangan> findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(StatusRecord statusRecord,String search,Pageable page);

    Page<Ruangan> findByStatusNotIn(StatusRecord hapus, Pageable page);
    Iterable<Ruangan> findByStatusNotIn(StatusRecord hapus);
}