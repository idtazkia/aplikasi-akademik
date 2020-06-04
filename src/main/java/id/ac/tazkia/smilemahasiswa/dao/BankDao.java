package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BankDao extends PagingAndSortingRepository<Bank, String> {

    Page<Bank> findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(List<StatusRecord> asList, String search, Pageable page);

    Page<Bank> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<Bank> findByStatus(StatusRecord statusRecord);

}
