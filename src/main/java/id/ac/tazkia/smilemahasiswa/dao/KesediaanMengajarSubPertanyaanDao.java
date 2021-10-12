package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KesediaanMengajarSubPertanyaan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KesediaanMengajarSubPertanyaanDao extends PagingAndSortingRepository<KesediaanMengajarSubPertanyaan, String> {
    List<KesediaanMengajarSubPertanyaan> findByStatus(StatusRecord statusRecord);
}
