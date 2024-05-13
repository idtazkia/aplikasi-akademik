package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kuesioner;
import id.ac.tazkia.smilemahasiswa.entity.KuesionerPertanyaan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KuesionerPertanyaanDao extends PagingAndSortingRepository<KuesionerPertanyaan, String> {
    List<KuesionerPertanyaan> findByStatusAndKuesionerOrderByUrutanAsc(StatusRecord statusRecord, Kuesioner kuesioner);

    Integer countByStatusAndKuesioner(StatusRecord statusRecord, Kuesioner kuesioner);
}
