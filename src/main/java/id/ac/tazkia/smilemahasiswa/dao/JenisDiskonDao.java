package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Diskon;
import id.ac.tazkia.smilemahasiswa.entity.JenisDiskon;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JenisDiskonDao extends PagingAndSortingRepository<JenisDiskon, String> {

    Page<JenisDiskon> findByStatusNotInAndNamaContainingIgnoreCaseOrderByNama(List<StatusRecord> asList, String search, Pageable page);

    Page<JenisDiskon> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    List<JenisDiskon> findByStatusOrderByNama(StatusRecord statusRecord);

}
