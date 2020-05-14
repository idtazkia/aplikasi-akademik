package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JenisTagihan;
import id.ac.tazkia.smilemahasiswa.entity.RequestPenangguhan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.bouncycastle.ocsp.Req;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestPenangguhanDao extends PagingAndSortingRepository<RequestPenangguhan, String> {

    Page<RequestPenangguhan> findByStatusNotInAndTanggalPenangguhanContainingIgnoreCaseOrderByTagihan(List<StatusRecord> asList, String search, Pageable page);

    Page<RequestPenangguhan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

}
