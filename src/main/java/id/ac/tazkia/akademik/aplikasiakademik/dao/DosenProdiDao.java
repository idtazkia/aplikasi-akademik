package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.DosenProdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DosenProdiDao extends PagingAndSortingRepository<DosenProdi, String> {

    Page<DosenProdi> findByIdDosenAndStatus(Dosen dosen, String status, Pageable pageable);
}

