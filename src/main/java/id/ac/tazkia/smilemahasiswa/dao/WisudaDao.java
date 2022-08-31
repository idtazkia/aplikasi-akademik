package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WisudaDao extends PagingAndSortingRepository<Wisuda, String> {
    Page<Wisuda> findByPeriodeWisudaAndStatusNotInOrderByStatusDescMahasiswaIdProdiAsc(PeriodeWisuda periodeWisuda, List<StatusApprove> statusApproves, Pageable page);
    Page<Wisuda> findByPeriodeWisudaOrderByStatusDescMahasiswaIdProdiAsc(PeriodeWisuda periodeWisuda, Pageable page);
    Page<Wisuda> findByPeriodeWisudaAndMahasiswaIdProdiAndStatusNotInOrderByStatusDesc(PeriodeWisuda periodeWisuda, Prodi prodi, List<StatusApprove> statusApproves,Pageable page);
    List<Wisuda> findByMahasiswaAndStatusNotIn(Mahasiswa mahasiswa, List<StatusApprove> statusApprove);
    Wisuda findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusApprove statusApprove);
    Wisuda findByMahasiswa(Mahasiswa mahasiswa);
    Wisuda findFirstByMahasiswaAndStatus(Mahasiswa mahasiswa,StatusApprove statusApprove);
    List<Wisuda> findByStatusAndPeriodeWisuda(StatusApprove status, PeriodeWisuda periodeWisuda);
}
