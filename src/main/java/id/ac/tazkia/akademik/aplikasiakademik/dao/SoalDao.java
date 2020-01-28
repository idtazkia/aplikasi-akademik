package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Soal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusApprove;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface SoalDao extends PagingAndSortingRepository<Soal, String> {

    Iterable<Soal> findByJadwal(Jadwal jadwal);
    Iterable<Soal> findByJadwalAndStatusAndStatusSoal(Jadwal jadwal,StatusRecord aktif,StatusRecord soal);
    Soal findByJadwalAndStatus(Jadwal jadwal, StatusRecord statusRecord);
    Soal findByJadwalAndStatusAndStatusApproveNotIn(Jadwal jadwal, StatusRecord statusRecord,StatusApprove statusApprove);
    Soal findByJadwalAndStatusAndStatusApprove(Jadwal jadwal, StatusRecord statusRecord, StatusApprove statusApprove);
    Soal findByJadwalAndStatusAndStatusApproveAndStatusSoal(Jadwal jadwal, StatusRecord statusRecord, StatusApprove statusApprove,StatusRecord soal);
}
