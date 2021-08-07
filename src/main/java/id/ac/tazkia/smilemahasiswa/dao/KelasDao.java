package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KelasDao extends PagingAndSortingRepository<Kelas,String> {
    Page<Kelas> findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(List<StatusRecord> asList, String search, Pageable page);

    Page<Kelas> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    @Query(value = "select a.id, a.nama_kelas from kelas as a inner join prodi as b on a.id_prodi = b.id where b.id = ?1 and a.angkatan = ?2", nativeQuery = true)
    List<Object[]> kelasAngktanProdi(String idProdi, String angkatan);

    List<Kelas> findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord statusRecord, String sp);

}
