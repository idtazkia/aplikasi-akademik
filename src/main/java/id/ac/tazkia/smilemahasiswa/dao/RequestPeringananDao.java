package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestPeringananDao extends PagingAndSortingRepository<RequestPeringanan, String> {

    Page<RequestPeringanan> findByStatusNotInAndTagihanMahasiswaNamaContainingIgnoreCaseOrderByTanggalPengajuanDesc(List<StatusRecord> asList, String search, Pageable page);

    Page<RequestPeringanan> findByStatusNotInOrderByTanggalPengajuanDesc(List<StatusRecord> asList, Pageable page);

    RequestPeringanan findByTagihanAndStatusAndStatusApproveNotIn(Tagihan tagihan, StatusRecord statusRecord, List<StatusApprove> asList);

    List<RequestPeringanan> findByTagihanTahunAkademikAndTagihanMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);

}
