package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.JenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.NilaiJenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JenisTagihanDao extends PagingAndSortingRepository<JenisTagihan, String> {

    Page<JenisTagihan> findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(List<StatusRecord> asList, String search, Pageable page);

    Page<JenisTagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<JenisTagihan> findByStatusOrderByNama(StatusRecord statusRecord);

    List<JenisTagihan> findByNamaAndStatus(String nama, StatusRecord statusRecord);
}
