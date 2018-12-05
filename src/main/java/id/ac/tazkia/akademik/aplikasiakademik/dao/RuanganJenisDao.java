package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.RuanganJenis;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RuanganJenisDao extends PagingAndSortingRepository<RuanganJenis, String> {
    Page<RuanganJenis> findByStatusNotIn(StatusRecord hapus, Pageable page);
    List<RuanganJenis> findByStatus(StatusRecord statusRecord);


}
