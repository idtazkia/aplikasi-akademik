package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Hari;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface HariDao extends PagingAndSortingRepository<Hari,String> {
    Iterable<Hari> findByNamaHariContainingIgnoreCaseOrNamaHariEngContainingIgnoreCase(String nama,String name);
    Hari findByNamaHariEngContainingIgnoreCase(String hari);
    List<Hari> findByStatus(StatusRecord statusRecord);
}

