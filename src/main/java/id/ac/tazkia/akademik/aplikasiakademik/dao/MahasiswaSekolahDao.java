package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MhswSekolahAsal;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaSekolahDao extends PagingAndSortingRepository<MhswSekolahAsal,String> {
}
