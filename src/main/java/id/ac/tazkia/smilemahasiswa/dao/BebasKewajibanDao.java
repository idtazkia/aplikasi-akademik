package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BebasKewajibanDao extends PagingAndSortingRepository<PendaftaranBebasKewajiban, String> {

    PendaftaranBebasKewajiban findByMahasiswa(Mahasiswa mahasiswa);

    PendaftaranBebasKewajiban findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveOrderByMahasiswaNim(StatusRecord status, StatusApprove statusApprove, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveInOrderByMahasiswaNim(StatusRecord status, List<StatusApprove> statusApprove, Pageable page);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNim(StatusRecord status, List<StatusApprove> statusApprove, String nama, String nim, Pageable page);

}
