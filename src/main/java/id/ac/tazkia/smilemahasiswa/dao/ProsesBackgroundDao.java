package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.ProsesBackground;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface ProsesBackgroundDao extends PagingAndSortingRepository<ProsesBackground, String> {

    List<ProsesBackground> findByStatusAndNamaProses(StatusRecord statusRecord, String namaProses);

    ProsesBackground findFirstByStatusOrderByTanggalMulaiDesc(StatusRecord statusRecord);

    ProsesBackground findFirstByStatusOrderByTanggalInputDesc(StatusRecord statusRecord);

    Page<ProsesBackground> findByStatusNotInOrderByTanggalInputDesc(StatusRecord statusRecord, Pageable page);

}
