package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ProsesBackgroundAbsensiDosen;
import id.ac.tazkia.smilemahasiswa.entity.ProsesBackgroundDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProsesBackgroundAbsensiDosenDao extends PagingAndSortingRepository<ProsesBackgroundAbsensiDosen,String> {

    List<ProsesBackgroundAbsensiDosen> findByStatusAndNamaProses(StatusRecord statusRecord, String namaProses);

    ProsesBackgroundAbsensiDosen findFirstByStatusOrderByTanggalMulaiDesc(StatusRecord statusRecord);

    ProsesBackgroundAbsensiDosen findFirstByStatusOrderByTanggalInputDesc(StatusRecord statusRecord);

    Page<ProsesBackgroundAbsensiDosen> findByStatusNotInOrderByTanggalInputDesc(List<StatusRecord> asList, Pageable page);

    Page<ProsesBackgroundAbsensiDosen> findByNamaDosenContainingIgnoreCaseAndStatusNotInOrderByTanggalInputDesc(String search,List<StatusRecord> asList, Pageable page);

}
