package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.DetailPertemuan;

import java.util.List;

public interface DetailPertemuanDao extends PagingAndSortingRepository<DetailPertemuan, String> {
    List<DetailPertemuan> findByStatus(StatusRecord statusRecord);

}
