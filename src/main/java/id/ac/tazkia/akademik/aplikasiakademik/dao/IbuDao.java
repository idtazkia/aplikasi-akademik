package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ibu;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IbuDao extends PagingAndSortingRepository<Ibu,String> {
    Ibu findByMahasiswa(Mahasiswa mhsw);
}
