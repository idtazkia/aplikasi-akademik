package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.KabupatenKota;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KokabDao extends PagingAndSortingRepository<KabupatenKota, String> {
}
