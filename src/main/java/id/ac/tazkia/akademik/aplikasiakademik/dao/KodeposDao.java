package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kodepos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KodeposDao extends PagingAndSortingRepository<Kodepos,String> {
    Page<Kodepos> findByKelurahanContainingIgnoreCaseOrKabupatenContainingIgnoreCaseOrKecamatanContainingIgnoreCaseOrPropinsiContainingIgnoreCase(String nama, String kabupaten, String kecamatan, String provinsi, Pageable page);
//    Page<Kodepos> findByKelurahanContainingIgnoreCase(String nama,Pageable page);
}
