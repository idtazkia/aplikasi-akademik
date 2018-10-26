package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.SesiKuliah;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SesiKuliahDao extends PagingAndSortingRepository<SesiKuliah, String> {

    @Query(value = "select s from SesiKuliah s where s.waktuMulai >= :mulai and s.waktuSelesai < :sampai")
    Iterable<SesiKuliah> cariSesiKuliah(@Param("mulai") LocalDateTime mulai, @Param("sampai") LocalDateTime sampai);
}
