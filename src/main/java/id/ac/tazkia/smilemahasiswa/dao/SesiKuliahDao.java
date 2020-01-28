package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosen;
import id.ac.tazkia.smilemahasiswa.entity.SesiKuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SesiKuliahDao extends PagingAndSortingRepository<SesiKuliah, String> {
    List<SesiKuliah> findByJadwalAndPresensiDosenStatusOrderByWaktuMulai(Jadwal jadwal, StatusRecord aktif);

    SesiKuliah findByPresensiDosen(PresensiDosen pd);
}
