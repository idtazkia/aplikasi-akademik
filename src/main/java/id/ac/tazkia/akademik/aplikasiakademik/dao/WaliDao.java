package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Wali;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface WaliDao extends PagingAndSortingRepository<Wali,String> {

}
