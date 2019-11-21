package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen, String> {
    List<PresensiDosen> findByStatusAndJadwal(StatusRecord status, Jadwal jadwal);
    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(Jadwal jadwal, Dosen dosen, TahunAkademik tahunAkademik, Hari hari,StatusRecord statusRecord);
    Iterable<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndStatusOrderByWaktuMasuk(Jadwal jadwal, Dosen dosen, TahunAkademik ta, StatusRecord statusRecord);
    Long countByJadwalAndDosenAndTahunAkademikAndStatus(Jadwal jadwal, Dosen dosen, TahunAkademik ta, StatusRecord statusRecord);
    Iterable<PresensiDosen> findByStatusAndWaktuMasukBetween(StatusRecord status, LocalDateTime mulai, LocalDateTime sampai);

    @Query("select count (*) from PresensiDosen pd where pd.status =:status and pd.jadwal.id = :jadwal")
    Long jumlahKehadiranDosen(@Param("status")StatusRecord statusRecord,@Param("jadwal")String jadwal);
}
