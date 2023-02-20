package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ProsesBackgroundNilaiIndividuElearning;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProsesBackgroundNilaiIndividuElearningDao extends PagingAndSortingRepository<ProsesBackgroundNilaiIndividuElearning,String> {

    List<ProsesBackgroundNilaiIndividuElearning> findByStatusAndNamaProses(StatusRecord statusRecord, String namaProses);

    ProsesBackgroundNilaiIndividuElearning findFirstByStatusOrderByTanggalMulaiDesc(StatusRecord statusRecord);

    ProsesBackgroundNilaiIndividuElearning findFirstByStatusOrderByTanggalInputDesc(StatusRecord statusRecord);

    Page<ProsesBackgroundNilaiIndividuElearning> findByStatusNotInOrderByTanggalInputDesc(List<StatusRecord> asList, Pageable page);


}
