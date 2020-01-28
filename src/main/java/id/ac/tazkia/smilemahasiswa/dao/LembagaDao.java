package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Lembaga;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface LembagaDao extends PagingAndSortingRepository<Lembaga,String> {
    Page<Lembaga> findByStatusNotInAndNamaLembagaContainingIgnoreCaseOrderByNamaLembaga(List<StatusRecord> asList, String search, Pageable page);

    Page<Lembaga> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Lembaga> findByStatus(StatusRecord aktif);
}
