package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Hari;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface HariDao extends PagingAndSortingRepository<Hari,String> {
    Hari findByNamaHariEngContainingIgnoreCase(String toString);

    List<Hari> findByStatus(StatusRecord aktif);
}
