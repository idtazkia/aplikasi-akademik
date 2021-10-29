package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.MemoKeuangan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MemoKeuanganDao extends PagingAndSortingRepository<MemoKeuangan, String> {

    Page<MemoKeuangan> findByStatusNotInAndNamaContainingIgnoreCaseOrderByCreateTime(List<StatusRecord> asList, String search, Pageable page);

    Page<MemoKeuangan> findByStatusNotInOrderByCreateTime(List<StatusRecord> asList, Pageable page);

    List<MemoKeuangan> findByTahunAkademikAndAngkatanAndStatusOrderByCreateTime(TahunAkademik tahunAkademik, String angkatan, StatusRecord statusRecord);

    Integer countAllByTahunAkademikAndAngkatanAndStatus(TahunAkademik tahunAkademik, String angkatan, StatusRecord status);

}
