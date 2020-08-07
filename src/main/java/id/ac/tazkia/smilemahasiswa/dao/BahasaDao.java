package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Bahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BahasaDao extends PagingAndSortingRepository<Bahasa, String> {

    List<Bahasa> findByStatusOrderByBahasa(StatusRecord statusRecord);

}
