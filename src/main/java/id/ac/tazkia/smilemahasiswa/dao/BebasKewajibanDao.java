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

    Page<PendaftaranBebasKewajiban> findByStatusOrderByTanggalRequest(StatusRecord status, Pageable page);

    Page<PendaftaranBebasKewajiban> findByStatusAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, String nama, String nim, Pageable page);

}
