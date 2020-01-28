package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prasyarat;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PrasyaratDao extends PagingAndSortingRepository<Prasyarat,String> {
    List<Prasyarat> findByMatakuliahKurikulumAndStatus(MatakuliahKurikulum matakuliahKurikulum, StatusRecord aktif);
}
