package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.JenisMk;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JenisMkDao extends PagingAndSortingRepository<JenisMk,String>{
    Page<JenisMk> findByStatus(StatusRecord status);
}
