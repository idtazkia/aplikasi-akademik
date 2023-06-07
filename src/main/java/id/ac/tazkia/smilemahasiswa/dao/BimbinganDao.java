package id.ac.tazkia.smilemahasiswa.dao;


import id.ac.tazkia.smilemahasiswa.entity.Bimbingan;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BimbinganDao extends PagingAndSortingRepository<Bimbingan, String> {

    List<Bimbingan> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord status);

}
