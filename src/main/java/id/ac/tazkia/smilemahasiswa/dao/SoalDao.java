package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SoalDao extends PagingAndSortingRepository<Soal, String> {
    Iterable<Soal> findByJadwalAndStatusAndStatusSoal(Jadwal jadwal, StatusRecord aktif, StatusRecord uas);

    Soal findByJadwalAndStatusAndStatusApproveAndStatusSoal(Jadwal jadwal, StatusRecord aktif, StatusApprove approved, StatusRecord uas);

    Soal findByJadwalAndStatusAndStatusApproveNotIn(Jadwal jadwal, StatusRecord aktif, List<StatusApprove> asList);
    Soal findByJadwalAndStatusAndStatusApproveNotInAndStatusSoal(Jadwal jadwal, StatusRecord aktif, List<StatusApprove> asList,StatusRecord statusRecord);

    Soal findByJadwalAndStatusAndStatusApprove(Jadwal jadwal, StatusRecord aktif, StatusApprove approved);
    Soal findByJadwalAndStatusSoalAndStatusAndStatusApproveIn(Jadwal jadwal, StatusRecord tipe, StatusRecord statusRecord,List<StatusApprove> statusApproves);

    List<Soal> findTopByStatusAndStatusSoalAndDosenOrderByTanggalUploadDesc(StatusRecord statusRecord, StatusRecord uas, Dosen dosen);

}
