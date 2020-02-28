package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kodepos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KodeposDao extends PagingAndSortingRepository<Kodepos,String> {
    Page<Kodepos> findByKelurahanContainingIgnoreCaseOrKabupatenContainingIgnoreCaseOrKecamatanContainingIgnoreCaseOrPropinsiContainingIgnoreCase(String nama, String kabupaten, String kecamatan, String provinsi, Pageable page);
//    Page<Kodepos> findByKelurahanContainingIgnoreCase(String nama,Pageable page);
}
