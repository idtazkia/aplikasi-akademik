package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MhswKost;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaKostDao extends PagingAndSortingRepository<MhswKost,String> {
}
