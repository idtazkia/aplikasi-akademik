package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jenjang;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenjangDao extends PagingAndSortingRepository<Jenjang,String> {
    Page<Jenjang> findByStatusNotIn(StatusRecord status, Pageable page);
    Iterable<Jenjang> findByStatusNotIn(StatusRecord s);
    Iterable<Jenjang> findByStatus(StatusRecord s);
    Page<Jenjang> findByStatusNotInAndNamaJenjangContainingIgnoreCaseOrderByNamaJenjang(StatusRecord status, String nama, Pageable page);
}
