package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen, String> {
    List<PresensiDosen> findByStatusAndJadwal(StatusRecord status, Jadwal jadwal);
    PresensiDosen findByJadwalAndDosenAndTahunAkademikAndJadwalHari(Jadwal jadwal, Dosen dosen, TahunAkademik tahunAkademik, Hari hari);
    PresensiDosen findByJadwalAndDosenAndTahunAkademikAndWaktuMasukBetween(Jadwal jadwal, Dosen dosen, TahunAkademik tahunAkademik,  LocalDateTime localDateTime,LocalDateTime jamSelesai);
}
