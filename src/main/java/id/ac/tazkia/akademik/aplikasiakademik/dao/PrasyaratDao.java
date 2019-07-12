package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.MatakuliahKurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prasyarat;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PrasyaratDao extends PagingAndSortingRepository<Prasyarat,String> {
    List<Prasyarat> findByMatakuliahKurikulumAndStatus(MatakuliahKurikulum matakuliahKurikulum, StatusRecord statusRecord);
}
