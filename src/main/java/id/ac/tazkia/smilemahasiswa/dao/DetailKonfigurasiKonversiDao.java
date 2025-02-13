package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.requestkonversi.DetailKonfigurasiKonversiDto;
import id.ac.tazkia.smilemahasiswa.entity.DetailKonfigurasiKonversi;
import id.ac.tazkia.smilemahasiswa.entity.KonfigurasiKonversi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DetailKonfigurasiKonversiDao extends PagingAndSortingRepository<DetailKonfigurasiKonversi, String> {
    List<DetailKonfigurasiKonversi> findByKonfigurasiKonversiOrderByNumberAscLevelNumberAsc(KonfigurasiKonversi konfigurasiKonversi);

    @Query(value = "SELECT \n" +
            "    nama,kategori, \n" +
            "    MAX(CASE WHEN level_number = 1 THEN nilai END) AS level1,\n" +
            "    MAX(CASE WHEN level_number = 2 THEN nilai END) AS level2,\n" +
            "    MAX(CASE WHEN level_number = 3 THEN nilai END) AS level3,\n" +
            "    MAX(CASE WHEN level_number = 4 THEN nilai END) AS level4,\n" +
            "    MAX(CASE WHEN level_number = 5 THEN nilai END) AS level5, \n" +
            "    MAX(level_number) AS number \n" +
            "FROM detail_konfigurasi_konversi WHERE id_konfigurasi_konversi = ?1 and status = 'AKTIF' \n" +
            "GROUP BY nama \n" +
            "order by number asc\n", nativeQuery = true)
    List<DetailKonfigurasiKonversiDto> getDetailKonversi(KonfigurasiKonversi konfigurasiKonversi);

    @Query(value = "SELECT DISTINCT level\n" +
            "FROM detail_konfigurasi_konversi\n" +
            "WHERE id_konfigurasi_konversi = '1'\n" +
            "AND status = 'AKTIF'\n" +
            "ORDER BY number ASC, level_number ASC", nativeQuery = true)
    List<String> getLevel(KonfigurasiKonversi konfigurasiKonversi);
}
