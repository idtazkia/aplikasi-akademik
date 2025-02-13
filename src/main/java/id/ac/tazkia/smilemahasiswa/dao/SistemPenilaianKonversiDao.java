package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KonfigurasiKonversi;
import id.ac.tazkia.smilemahasiswa.entity.SistemPenilaianKonversi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SistemPenilaianKonversiDao extends PagingAndSortingRepository<SistemPenilaianKonversi, String> {
    List<SistemPenilaianKonversi> findByKonfigurasiKonversiOrderByOrderAsc(KonfigurasiKonversi konfigurasiKonversi);

    @Query(value = "SELECT * \n" +
            "FROM sistem_penilaian_konversi \n" +
            "WHERE id_konfigurasi_konversi = ?1 \n" +
            "AND ?2 BETWEEN point_bawah AND point_atas \t", nativeQuery = true)
    SistemPenilaianKonversi findByPointBawahAndPointAtas(KonfigurasiKonversi konfigurasiKonversi, Integer point);

    @Query(value = "SELECT * FROM sistem_penilaian_konversi where id_konfigurasi_konversi = ?1 and sks = ?2 and status = 'AKTIF'", nativeQuery = true)
    SistemPenilaianKonversi cekPenilaianKonversi(KonfigurasiKonversi konfigurasiKonversi, Integer sks);
}
