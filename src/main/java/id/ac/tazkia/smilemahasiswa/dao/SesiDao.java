package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Sesi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalTime;
import java.util.List;

public interface SesiDao extends PagingAndSortingRepository<Sesi,String> {
    List<Sesi> findBySks(Integer sks);

    List<Sesi> findBySesiInAndSks(List<String> jadwal, Integer sks);

    Sesi findByJamMulaiAndJamSelesaiAndSks(LocalTime masuk, LocalTime selesai,Integer sks);
}
