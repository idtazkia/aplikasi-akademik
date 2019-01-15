package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Pekerjaan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PekerjaanDao extends PagingAndSortingRepository<Pekerjaan,String> {
}
