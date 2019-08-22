package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ipk;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IpkDao extends PagingAndSortingRepository<Ipk,String> {
    Ipk findByMahasiswa(Mahasiswa mahasiswa);
}
