package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MatakuliahDao extends PagingAndSortingRepository<Matakuliah,String> {
    Page<Matakuliah> findByStatusNotInOrderByNamaMatakuliahAsc(List<StatusRecord> status, Pageable page);

    Page<Matakuliah> findByNamaMatakuliahContainingIgnoreCase(String search, Pageable page);

    Page<Matakuliah> findByNamaMatakuliahContainingIgnoreCaseOrNamaMatakuliahEnglishContainingIgnoreCase(String nama,String name,Pageable page);
}
