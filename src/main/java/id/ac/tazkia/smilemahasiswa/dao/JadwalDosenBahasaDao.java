package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.JadwalDosenBahasa;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalDosenBahasaDao extends PagingAndSortingRepository<JadwalDosenBahasa, String> {
    List<JadwalDosenBahasa> findByJadwalBahasa(JadwalBahasa jadwalBahasa);
}
