package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.JenisTinggal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenisTinggalDao extends PagingAndSortingRepository<JenisTinggal,String> {
    Iterable<JenisTinggal> findByStatus(StatusRecord statusRecord);
}
