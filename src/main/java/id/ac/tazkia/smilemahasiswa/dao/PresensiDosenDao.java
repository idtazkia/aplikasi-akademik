package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;


public interface PresensiDosenDao extends PagingAndSortingRepository<PresensiDosen,String> {
    Long countByStatusAndJadwal(StatusRecord aktif, Jadwal jadwal);

    List<PresensiDosen> findByJadwalAndDosenAndTahunAkademikAndJadwalHariAndStatus(Jadwal j, Dosen d, TahunAkademik tahunAkademik, Hari hari, StatusRecord aktif);
}
