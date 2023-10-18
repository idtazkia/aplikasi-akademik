package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.FileMbkm;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FileMbkmDao extends PagingAndSortingRepository<FileMbkm, String> {

    List<FileMbkm> findByKrsDetailAndStatusOrderByWaktuInput(KrsDetail krsDetail, StatusRecord status);

    List<FileMbkm> findByStatusOrderByWaktuInput(StatusRecord status);

}
