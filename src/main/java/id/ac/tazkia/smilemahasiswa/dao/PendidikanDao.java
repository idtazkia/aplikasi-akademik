package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Pendidikan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PendidikanDao extends PagingAndSortingRepository<Pendidikan,String> {
}
