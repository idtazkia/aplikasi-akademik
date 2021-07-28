package id.ac.tazkia.smilemahasiswa.dao;


import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PraKrsSp;
import id.ac.tazkia.smilemahasiswa.entity.RefundSp;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RefundSpDao extends PagingAndSortingRepository<RefundSp, String> {

    Page<RefundSp> findByStatusNotInAndNamaBankContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrMahasiswaNamaContainingIgnoreCaseOrderByMahasiswaNim(List<StatusRecord> asList, String bank, String nim, String mahasiswa, Pageable page);

    Page<RefundSp> findByStatusNotInOrderByMahasiswaNim(List<StatusRecord> asList, Pageable page);

    RefundSp findByPraKrsSpAndMahasiswaAndStatus(PraKrsSp praKrsSp, Mahasiswa mahasiswa, StatusRecord statusRecord);

}
