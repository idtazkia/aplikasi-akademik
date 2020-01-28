package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Pekerjaan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PekerjaanDao extends PagingAndSortingRepository<Pekerjaan,String> {
}
