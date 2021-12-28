package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.JenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.NilaiJenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface JenisTagihanDao extends PagingAndSortingRepository<JenisTagihan, String> {

    Page<JenisTagihan> findByStatusNotInAndNamaContainingIgnoreCaseOrderByKode(List<StatusRecord> asList, String search, Pageable page);

    Page<JenisTagihan> findByStatusNotInOrderByKode(List<StatusRecord> asList, Pageable page);

    List<JenisTagihan> findByStatusOrderByKode(StatusRecord statusRecord);

    JenisTagihan findByKodeAndStatus(String kode, StatusRecord statusRecord);

    List<JenisTagihan> findByStatusAndIdNotInOrderByKode(StatusRecord statusRecord, List<String> id);

}
