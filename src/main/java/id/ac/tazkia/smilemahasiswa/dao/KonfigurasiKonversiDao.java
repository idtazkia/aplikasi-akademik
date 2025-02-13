package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KonfigurasiKonversi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KonfigurasiKonversiDao extends PagingAndSortingRepository<KonfigurasiKonversi, String> {
    List<KonfigurasiKonversi> findByJenis(KonfigurasiKonversi.jenis jenis);
}
