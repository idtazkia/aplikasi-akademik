package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.EdomMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EdomMahasiswaDao extends PagingAndSortingRepository<EdomMahasiswa, String> {

    List<EdomMahasiswa> findByStatusOrderByEdomQuestionNomor(StatusRecord statusRecord);

}
