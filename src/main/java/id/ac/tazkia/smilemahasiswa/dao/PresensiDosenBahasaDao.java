package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PresensiDosenBahasaDao extends PagingAndSortingRepository<PresensiDosenBahasa, String> {
    List<PresensiDosenBahasa> findByJadwalBahasaAndStatusOrderByPertemuanKeAsc(JadwalBahasa jadwalBahasa, StatusRecord status);

    @Query(value = "SELECT COUNT(*) AS jumlah_presensi FROM presensi_dosen_bahasa WHERE id_jadwal_bahasa = ?1 AND status = 'AKTIF'", nativeQuery = true)
    Integer totalPertertemuan(JadwalBahasa jadwalBahasa);
}
