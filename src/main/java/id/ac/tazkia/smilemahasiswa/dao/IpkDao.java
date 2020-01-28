package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Ipk;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IpkDao extends PagingAndSortingRepository<Ipk, String> {
    Ipk findByMahasiswa(Mahasiswa mahasiswa);
}
