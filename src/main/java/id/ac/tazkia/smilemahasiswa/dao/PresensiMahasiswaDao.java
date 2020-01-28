package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PresensiMahasiswaDao extends PagingAndSortingRepository<PresensiMahasiswa, String> {
    List<PresensiMahasiswa> findBySesiKuliahAndStatus(SesiKuliah sesiKuliah, StatusRecord aktif);

    List<PresensiMahasiswa> findBySesiKuliah(SesiKuliah sesiKuliah);

    PresensiMahasiswa findByMahasiswaAndSesiKuliahAndStatus(Mahasiswa m, SesiKuliah sesiKuliah, StatusRecord aktif);

    Long countByStatusPresensiNotInAndKrsDetailAndStatus(List<StatusPresensi> statusp, KrsDetail krsDetail, StatusRecord aktif);

    Long countByStatusAndSesiKuliahPresensiDosenStatusAndStatusPresensiNotInAndMahasiswaAndKrsDetailJadwalMatakuliahKurikulumMatakuliahKodeMatakuliahContainingIgnoreCase(StatusRecord aktif, StatusRecord aktif1, List<StatusPresensi> statusp, Mahasiswa mahasiswa, String sds);
}
