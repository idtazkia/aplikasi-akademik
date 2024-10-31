package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.DetailJadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.stream.LongStream;

public interface DetailJadwalBahasaDao extends PagingAndSortingRepository<DetailJadwalBahasa, String> {
    DetailJadwalBahasa findByJadwalBahasaAndSesi(JadwalBahasa jadwalBahasa, String sesi);

    List<DetailJadwalBahasa> findByJadwalBahasa(JadwalBahasa jadwalBahasa);
}
