package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.RuanganJenis;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RuangJenisDao extends PagingAndSortingRepository<RuanganJenis,String> {
    List<RuanganJenis> findByStatus(StatusRecord aktif);
}
