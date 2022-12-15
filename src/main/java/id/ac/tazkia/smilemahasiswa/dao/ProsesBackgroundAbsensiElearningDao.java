package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ProsesBackground;
import id.ac.tazkia.smilemahasiswa.entity.ProsesBackgroundAbsensiElearning;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProsesBackgroundAbsensiElearningDao extends PagingAndSortingRepository<ProsesBackgroundAbsensiElearning, String> {

    List<ProsesBackgroundAbsensiElearning> findByStatusAndNamaProses(StatusRecord statusRecord, String namaProses);

    ProsesBackgroundAbsensiElearning findFirstByStatusOrderByTanggalMulaiDesc(StatusRecord statusRecord);

    ProsesBackgroundAbsensiElearning findFirstByStatusOrderByTanggalInputDesc(StatusRecord statusRecord);

    Page<ProsesBackgroundAbsensiElearning> findByStatusNotInOrderByTanggalInputDesc(List<StatusRecord> asList, Pageable page);

}
