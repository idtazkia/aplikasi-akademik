package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kampus;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KampusDao extends PagingAndSortingRepository<Kampus, String> {
    List<Kampus> findByStatus(StatusRecord aktif);

    Page<Kampus> findByStatusNotInAndNamaKampusContainingIgnoreCaseOrderByNamaKampus(List<StatusRecord> asList, String search, Pageable page);

    Page<Kampus> findByStatusNotIn(List<StatusRecord> asList, Pageable page);
}
