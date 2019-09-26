package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Hari;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface HariDao extends PagingAndSortingRepository<Hari,String> {
    Iterable<Hari> findByNamaHariContainingIgnoreCaseOrNamaHariEngContainingIgnoreCase(String nama,String name);
    Hari findByNamaHariEngContainingIgnoreCase(String hari);
}
