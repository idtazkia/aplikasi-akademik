package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TahunProdiDao extends PagingAndSortingRepository<TahunAkademikProdi, String> {

    List<TahunAkademikProdi> findByStatus(StatusRecord aktif);

    List<TahunAkademikProdi> findByTahunAkademik(TahunAkademik akademik);

    TahunAkademikProdi findByStatusAndTahunAkademikAndProdi(StatusRecord status, TahunAkademik tahun, Prodi prodi);


    TahunAkademikProdi findByStatusAndProdi(StatusRecord statusRecord, Prodi prodi);

    Iterable<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikDesc(List<StatusRecord >hapus);

    TahunAkademikProdi findByTahunAkademikAndStatusAndProdi(TahunAkademik tahunAkademik,StatusRecord statusRecord,Prodi prodi);

    Page<TahunAkademikProdi> findByStatusOrderByTahunAkademikKodeTahunAkademikDesc(StatusRecord statusRecord, Pageable page);

    Page<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikKodeTahunAkademik(List<StatusRecord> statusRecords, Pageable page);

    Page<TahunAkademikProdi> findByTahunAkademikKodeTahunAkademikContainingOrderByTahunAkademikKodeTahunAkademikDesc(String ada, Pageable page);

    @Modifying
    @Query(value = "update tahun_akademik_prodi set status = 'NONAKTIF' where id <> ?1 and id_prodi = ?2 and status = 'AKTIF'" , nativeQuery = true)
    int updateDataTahunAkademikProdi(String idTahunAkademikProdi, String idProdi);

    @Modifying
    @Query("UPDATE TahunAkademikProdi c SET c.status = :status WHERE c.id = :tahunAKademikProdi and c.prodi = :prodi")
    int updateTahunAKademikProdi2(@Param("tahunAKademikProdi") String tahunAKademikProdi, @Param("prodi") Prodi prodi, @Param("status") StatusRecord statusRecord);


    List<TahunAkademikProdi> findByTahunAkademikAndProdiAndStatus(TahunAkademik tahunAkademik, Prodi prodi, StatusRecord statusRecord);

    @Query(value = "select a.id, c.nama_prodi, b.kode_tahun_akademik, b.nama_tahun_akademik, b.tahun, a.status from tahun_akademik_prodi as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join prodi as c on c.id = a.id_prodi where b.status = 'AKTIF' order by c.nama_prodi asc", nativeQuery = true)
    List<Object[]> listTahaunAkademikProdi();

    @Query(value = "select a.id, c.nama_prodi, b.kode_tahun_akademik, b.nama_tahun_akademik, b.tahun, a.status from tahun_akademik_prodi as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join prodi as c on c.id = a.id_prodi where b.id = ?1 order by c.nama_prodi asc", nativeQuery = true)
    List<Object[]> tahunAkademikProdiGet(TahunAkademik tahunAkademik);

    TahunAkademikProdi findByTahunAkademikAndProdi(TahunAkademik tahunAkademik, Prodi idProdi);
}
