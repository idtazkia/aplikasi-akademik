package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.bouncycastle.ocsp.Req;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestCicilanDao extends PagingAndSortingRepository<RequestCicilan, String> {

    Page<RequestCicilan> findByStatusNotInAndTagihanContainingIgnoreCase(List<StatusRecord> asList, String search, Pageable page);

    Page<RequestCicilan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Page<RequestCicilan> findByStatusNotInAndTagihan(List<StatusRecord> asList, Pageable page, Tagihan tagihan);

    Long countRequestCicilanByTagihanAndStatus(Tagihan tagihan, StatusRecord statusRecord);

    RequestCicilan findByTagihanAndStatus(Tagihan tagihan, StatusRecord statusRecord);

    RequestCicilan findByTagihanAndStatusCicilan(Tagihan tagihan, StatusTagihan st);

}
