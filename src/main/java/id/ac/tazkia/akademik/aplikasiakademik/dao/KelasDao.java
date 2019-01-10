package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KelasDao extends PagingAndSortingRepository<Kelas,String> {

    Iterable<Kelas> findByStatusNotIn(StatusRecord statusRecord);
}
