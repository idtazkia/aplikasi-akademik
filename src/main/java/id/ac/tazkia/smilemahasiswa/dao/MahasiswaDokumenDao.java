package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaDokumen;

public interface MahasiswaDokumenDao extends PagingAndSortingRepository<MahasiswaDokumen, String> {
    List<MahasiswaDokumen> findByMahasiswa(Mahasiswa mahasiswa);
}
