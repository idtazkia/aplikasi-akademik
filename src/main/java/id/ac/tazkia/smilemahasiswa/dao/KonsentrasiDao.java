package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Konsentrasi;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KonsentrasiDao extends PagingAndSortingRepository<Konsentrasi, String> {
    Iterable<Konsentrasi> findByStatus(StatusRecord status);

    Page<Konsentrasi> findByStatusNotInAndAndNamaKonsentrasiContainingIgnoreCaseOrderByNamaKonsentrasi(List<StatusRecord> asList, String search, Pageable page);

    Page<Konsentrasi> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    @Query(value = "select a.id, a.kode_konsentrasi, a.nama_konsentrasi from konsentrasi as a inner join prodi as b on a.id_prodi = b.id where b.id = ?1 and a.status = 'AKTIF'", nativeQuery = true)
    List<Object[]> konsentrasiProdi(String prodi);
}
