package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Dokumen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DokumenDao extends PagingAndSortingRepository<Dokumen, String>, CrudRepository<Dokumen, String> {

    Page<Dokumen> findByStatusOrderByStartDateAsc(StatusRecord status, Pageable pageable);

}
