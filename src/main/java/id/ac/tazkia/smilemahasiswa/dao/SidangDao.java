package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SidangDao extends PagingAndSortingRepository<Sidang, String> {
    Sidang findBySeminarAndStatusAndStatusSidangAndAkademik(Seminar seminar, StatusRecord status, StatusApprove statusSidang,StatusApprove statusAppr);
    List<Sidang> findBySeminarAndStatusNotInOrderByTanggalInputDesc(Seminar seminar,List<StatusRecord> statusRecords);
    Sidang findBySeminarAndStatus(Seminar seminar,StatusRecord statusApprove);
    Page<Sidang> findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikNotInAndStatus(TahunAkademik tahunAkademik, Prodi prodi, List<StatusApprove> statusApproves, StatusRecord statusRecord, Pageable page);

}
