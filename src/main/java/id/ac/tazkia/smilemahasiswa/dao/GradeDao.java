package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Grade;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface GradeDao extends PagingAndSortingRepository<Grade,String> {
    List<Grade> findByStatus(StatusRecord aktif);
}
