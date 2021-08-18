package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KurikulumDao extends PagingAndSortingRepository<Kurikulum, String> {
    Page<Kurikulum> findByStatusInAndProdi(List<StatusRecord> status, Prodi prodi, Pageable page);
    List<Kurikulum> findByStatusInAndProdi(List<StatusRecord> status, Prodi prodi);

    List<Kurikulum> findByStatusNotIn(List<StatusRecord> asList);

    List<Kurikulum> findByProdiAndStatusNotInAndIdNotIn(Prodi prodi, List<StatusRecord> asList, List<String> id);
    List<Kurikulum> findByProdiAndStatusNotIn(Prodi prodi, StatusRecord statusRecord);

    List<Kurikulum> findByStatusNotInAndProdiAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(List<StatusRecord> asList, Prodi prodi, String search);

    @Modifying
    @Query(value = "update kurikulum set status = 'NONAKTIF' where id_prodi = ?1 and status = 'AKTIF'", nativeQuery = true)
    void nonaktifKurikulum(Prodi prodi);

    Kurikulum findByProdiAndStatus(Prodi prodi,StatusRecord statusRecord);
}
