package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagihanDao extends PagingAndSortingRepository<Tagihan, String> {

    Page<Tagihan> findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(List<StatusRecord> asList, String search, Pageable page);

    Page<Tagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Tagihan findByNomor(String nomor);

    List<Tagihan> findByTahunAkademikAndNilaiJenisTagihanAndStatus(TahunAkademik tahunAkademik, NilaiJenisTagihan nilaiJenisTagihan, StatusRecord statusRecord);

}
