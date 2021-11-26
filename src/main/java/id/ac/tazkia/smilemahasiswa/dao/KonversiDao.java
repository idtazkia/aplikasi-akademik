package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Konversi;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KonversiDao extends PagingAndSortingRepository<Konversi, String> {

    Konversi findByKrsDetailAndStatus(KrsDetail krsDetail, StatusRecord status);

}
