package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa,String> {
}
