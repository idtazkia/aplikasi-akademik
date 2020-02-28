package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JenisTinggal;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenisTinggalDao extends PagingAndSortingRepository<JenisTinggal,String> {
    Iterable<JenisTinggal> findByStatus(StatusRecord statusRecord);
}
