package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelurahan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KelurahanDao extends PagingAndSortingRepository<Kelurahan,String> {
    Page<Kelurahan> findByNamaContainingIgnoreCaseOrKabupatenKotaNamaContainingIgnoreCaseOrKecamatanNamaContainingIgnoreCaseOrProvinsiNamaContainingIgnoreCase(String nama, String kokab, String kecamatan, String provinsi, Pageable pageable);

    Page<Kelurahan> findByNamaContainingIgnoreCase(String nama, Pageable page);
    Page<Kelurahan> findByKabupatenKotaNamaContainingIgnoreCase(String nama, Pageable page);
}
