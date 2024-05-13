package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KuesionerPertanyaan;
import id.ac.tazkia.smilemahasiswa.entity.KuesionerPilihanGanda;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KuesionerPilihanGandaDao extends PagingAndSortingRepository<KuesionerPilihanGanda, String> {
    List<KuesionerPilihanGanda> findByStatusAndKuesionerPertanyaanOrderByUrutanAsc(StatusRecord statusRecord, KuesionerPertanyaan kuesionerPertanyaan);
}
