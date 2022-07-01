package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.PeriodeWisuda;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.Wisuda;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeriodeWisudaDao extends PagingAndSortingRepository<PeriodeWisuda, String> {

    Page<PeriodeWisuda> findByStatusNotInAndNamaContainingIgnoreCase(List<StatusRecord> asList, String search, Pageable page);

    List<PeriodeWisuda> findByStatusNotInOrderByTanggalWisudaDesc(List<StatusRecord> statusRecord);

    Page<PeriodeWisuda> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    PeriodeWisuda findByStatus(StatusRecord statusRecord);

}
