package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Agama;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AgamaDao extends PagingAndSortingRepository<Agama, String> {
    Iterable<Agama> findByStatus(StatusRecord statusRecord);
}
