package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.Soal;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SoalDao extends PagingAndSortingRepository<Soal, String> {
    Iterable<Soal> findByJadwalAndStatusAndStatusSoal(Jadwal jadwal, StatusRecord aktif, StatusRecord uas);

    Soal findByJadwalAndStatusAndStatusApproveAndStatusSoal(Jadwal jadwal, StatusRecord aktif, StatusApprove approved, StatusRecord uas);

    Soal findByJadwalAndStatusAndStatusApproveNotIn(Jadwal jadwal, StatusRecord aktif, List<StatusApprove> asList);
}
