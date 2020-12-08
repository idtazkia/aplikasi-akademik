package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.RequestCicilan;
import id.ac.tazkia.smilemahasiswa.entity.RequestPenangguhan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestCicilanDao extends PagingAndSortingRepository<RequestCicilan, String> {

    Page<RequestCicilan> findByStatusNotInAndBanyakCicilanContainingIgnoreCase(List<StatusRecord> asList, String search, Pageable page);

    Page<RequestCicilan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    RequestCicilan findByTagihanAndStatus(Tagihan tagihan, StatusRecord statusRecord);

}
