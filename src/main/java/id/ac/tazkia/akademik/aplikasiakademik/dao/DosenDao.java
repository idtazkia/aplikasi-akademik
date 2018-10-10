package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {
}
