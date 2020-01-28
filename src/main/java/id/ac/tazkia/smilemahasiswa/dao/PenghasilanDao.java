package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Penghasilan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PenghasilanDao extends PagingAndSortingRepository<Penghasilan,String> {
}
