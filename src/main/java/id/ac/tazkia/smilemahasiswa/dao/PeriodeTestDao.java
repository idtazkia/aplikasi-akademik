package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.entity.PeriodeTest;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeriodeTestDao extends PagingAndSortingRepository<PeriodeTest, String> {
    Page<PeriodeTest> findByStatusInOrderByStatusAsc(List<StatusRecord> statusRecords, Pageable pageable);

    PeriodeTest findByStatusAndJenisTest(StatusRecord statusRecord, JenisTest jenisTest);

    List<PeriodeTest> findAllByJenisTestOrderByTanggalUjianDesc(JenisTest jenisTest);

    List<PeriodeTest> findAllByOrderByTanggalUjianDesc();
}
