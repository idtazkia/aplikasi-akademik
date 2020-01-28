package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Ruangan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RuanganDao extends PagingAndSortingRepository<Ruangan, String> {
    Iterable<Ruangan> findByStatus(StatusRecord aktif);
}
