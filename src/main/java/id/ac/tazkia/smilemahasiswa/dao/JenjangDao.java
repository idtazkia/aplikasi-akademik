package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jenjang;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JenjangDao extends PagingAndSortingRepository<Jenjang,String> {
    Page<Jenjang> findByStatusNotInAndNamaJenjangContainingIgnoreCaseOrderByNamaJenjang(List<StatusRecord> asList, String search, Pageable page);

    Page<Jenjang> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Jenjang> findByStatus(StatusRecord aktif);
}
