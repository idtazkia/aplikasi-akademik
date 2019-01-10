package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {

    Long countDosenByStatus(StatusRecord statusRecord);
    Iterable<Dosen> findByStatusNotIn(StatusRecord statusRecord);

}
