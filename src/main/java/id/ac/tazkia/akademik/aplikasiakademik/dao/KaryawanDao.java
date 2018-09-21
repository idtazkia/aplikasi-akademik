package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan,String> {
}
