package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.MatakuliahKurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MatakuliahKurikulumDao extends PagingAndSortingRepository<MatakuliahKurikulum, String> {

    Page<MatakuliahKurikulum> findByStatusNotIn(StatusRecord statusRecord, Pageable page);

    List<MatakuliahKurikulum> findByStatusNotIn(StatusRecord statusRecord);
}
