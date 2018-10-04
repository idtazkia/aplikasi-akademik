package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Ayah;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AyahDao extends PagingAndSortingRepository <Ayah,String> {
    Ayah findByMahasiswa(Mahasiswa mahasiswa);
}
