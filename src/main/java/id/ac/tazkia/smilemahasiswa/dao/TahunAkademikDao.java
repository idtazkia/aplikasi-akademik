package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TahunAkademikDao extends PagingAndSortingRepository<TahunAkademik, String> {
    TahunAkademik findByStatus(StatusRecord aktif);

    TahunAkademik findByKodeTahunAkademikAndJenis(String kode,StatusRecord statusRecord);

    Iterable<TahunAkademik> findByStatusNotInOrderByTahunDesc(List<StatusRecord> statusRecords);

    @Query(value = "select tahun.id,tahun.nama_tahun_akademik from tahun_akademik as tahun where status not in(?1) order by nama_tahun_akademik desc", nativeQuery = true)
    List<Object[]> cariTahunAkademik(List<StatusRecord> statusRecord);

    Page<TahunAkademik> findByStatusInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(List<StatusRecord> status, String search, Pageable page);

    @Query("select ta from TahunAkademik ta where ta.status not in (:status) order by ta.kodeTahunAkademik desc")
    Page<TahunAkademik> cariTahunAkademik(@Param("status") StatusRecord hapus, Pageable page);

    List<TahunAkademik> findByStatusNotInOrderByNamaTahunAkademikDesc(List<StatusRecord> asList);
}
