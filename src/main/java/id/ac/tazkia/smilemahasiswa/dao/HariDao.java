package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Hari;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HariDao extends PagingAndSortingRepository<Hari,String> {
    Hari findByNamaHariEngContainingIgnoreCase(String toString);
}
