package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kuesioner;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KuesionerDao extends PagingAndSortingRepository<Kuesioner, String> {
    Page<Kuesioner> findByStatus(StatusRecord statusRecord, Pageable pageable);
}
