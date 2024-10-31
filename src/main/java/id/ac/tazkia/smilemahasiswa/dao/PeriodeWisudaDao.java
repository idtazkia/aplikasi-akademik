package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.PeriodeWisuda;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;

public interface PeriodeWisudaDao extends PagingAndSortingRepository<PeriodeWisuda, String> {

    Page<PeriodeWisuda> findByStatusNotInAndNamaContainingIgnoreCase(List<StatusRecord> asList, String search, Pageable page);

    List<PeriodeWisuda> findByStatusNotInOrderByNumberDesc(List<StatusRecord> statusRecord);

    Page<PeriodeWisuda> findByStatusNotInOrderByNumberDesc(List<StatusRecord> asList, Pageable page);

    PeriodeWisuda findByStatus(StatusRecord statusRecord);

    @Query(value = "SELECT max(number) as number FROM periode_wisuda where status not in ('HAPUS') ", nativeQuery = true)
    Integer getLastNumber();

    @Modifying
    @Query(value = "update periode_wisuda set number = number - 1 where number > ?1 and status not in ('HAPUS')", nativeQuery = true)
    void generateNumber(Integer number);

    List<PeriodeWisuda> findByNumberGreaterThanAndStatusNotIn(Integer number, List<StatusRecord> status);

}
