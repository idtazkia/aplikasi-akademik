package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Kampus;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KampusDao extends PagingAndSortingRepository<Kampus, String> {
    Page<Kampus> findByStatusNotInAndNamaKampusContainingIgnoreCaseOrderByNamaKampus(StatusRecord s,String nama,Pageable pageable);

    Page<Kampus> findByStatusNotIn(StatusRecord hapus, Pageable page);
    List<Kampus> findByStatus(StatusRecord statusRecord);
}
