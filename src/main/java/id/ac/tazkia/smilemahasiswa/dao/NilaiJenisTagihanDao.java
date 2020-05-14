package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.NilaiJenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface NilaiJenisTagihanDao extends PagingAndSortingRepository<NilaiJenisTagihan, String> {

    Page<NilaiJenisTagihan> findByStatusNotInAndJenisTagihanContainingIgnoreCaseOrderByJenisTagihan(List<StatusRecord> asList, String search, Pageable page);

    Page<NilaiJenisTagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<NilaiJenisTagihan> findByStatusOrderByJenisTagihan(StatusRecord statusRecord);

    List<NilaiJenisTagihan> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord statusRecord);

}
