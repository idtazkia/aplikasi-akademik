package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalDosenDao extends PagingAndSortingRepository<JadwalDosen, String> {

    Iterable<JadwalDosen> findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord status, TahunAkademik tahunAkademik, Dosen dosen);
    JadwalDosen findByJadwalAndDosenAndStatusJadwalDosen(Jadwal jadwal,Dosen dosen,StatusJadwalDosen statusJadwalDosen);
    List<JadwalDosen> findByJadwalAndStatusJadwalDosen(Jadwal jadwal, StatusJadwalDosen statusJadwalDosen);
}
