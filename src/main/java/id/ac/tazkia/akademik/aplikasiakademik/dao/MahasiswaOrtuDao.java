package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MhswOrtu;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaOrtuDao extends PagingAndSortingRepository<MhswOrtu,String> {
}
