package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen, String> {
    List<PresensiDosen> findByStatusAndJadwal(StatusRecord status, Jadwal jadwal);
    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHari(Jadwal jadwal, Dosen dosen, TahunAkademik tahunAkademik, Hari hari);
}
