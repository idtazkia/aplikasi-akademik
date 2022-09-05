package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ProsesBackground;
import id.ac.tazkia.smilemahasiswa.entity.ProsesBackgroundDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProsesBackgroundDosenDao extends PagingAndSortingRepository<ProsesBackgroundDosen, String> {

    List<ProsesBackgroundDosen> findByStatusAndNamaProses(StatusRecord statusRecord, String namaProses);

    ProsesBackgroundDosen findFirstByStatusOrderByTanggalMulaiDesc(StatusRecord statusRecord);

    ProsesBackgroundDosen findFirstByStatusOrderByTanggalInputDesc(StatusRecord statusRecord);

    Page<ProsesBackgroundDosen> findByStatusNotInOrderByTanggalInputDesc(List<StatusRecord> asList, Pageable page);

}
