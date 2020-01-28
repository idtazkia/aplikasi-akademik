package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Agama;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AgamaDao extends PagingAndSortingRepository<Agama, String> {
    List<Agama> findByStatus(StatusRecord statusRecord);
}
