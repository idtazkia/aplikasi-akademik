package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JabatanDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JabatanDao extends PagingAndSortingRepository<JabatanDosen, String> {
    List<JabatanDosen> findByStatus(StatusRecord status);
}
