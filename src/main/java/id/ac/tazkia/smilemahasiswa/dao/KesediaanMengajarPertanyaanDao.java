package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KesediaanMengajarPertanyaan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KesediaanMengajarPertanyaanDao extends PagingAndSortingRepository<KesediaanMengajarPertanyaan, String> {
    List<KesediaanMengajarPertanyaan> findByStatusNotInOrderByUrutanAsc(List<StatusRecord> asList);

    List<KesediaanMengajarPertanyaan> findByStatusOrderByUrutanAsc(StatusRecord statusRecord);

}
