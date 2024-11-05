package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalBahasaDao extends PagingAndSortingRepository<JadwalBahasa, String> {
    List<JadwalBahasa> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord status);
}
