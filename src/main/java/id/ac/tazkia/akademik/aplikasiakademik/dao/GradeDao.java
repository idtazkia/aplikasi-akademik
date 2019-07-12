package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Grade;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface GradeDao extends PagingAndSortingRepository<Grade, String> {
    Iterable<Grade> findByStatus(StatusRecord statusRecord);

}
