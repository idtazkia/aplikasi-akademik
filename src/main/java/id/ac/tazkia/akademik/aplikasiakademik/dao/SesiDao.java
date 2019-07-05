package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Sesi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SesiDao extends PagingAndSortingRepository<Sesi,String> {
    List<Sesi> findBySesiNotInAndSks(List<Jadwal> jadwals,Integer sks);
    List<Sesi> findBySks(Integer sks);
}
