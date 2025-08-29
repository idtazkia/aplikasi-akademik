package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;

public interface TahunProdiDao extends PagingAndSortingRepository<TahunAkademikProdi, String> {

    List<TahunAkademikProdi> findByStatus(StatusRecord aktif);

    List<TahunAkademikProdi> findByTahunAkademik(TahunAkademik akademik);

    TahunAkademikProdi findByStatusAndTahunAkademikAndProdi(StatusRecord status, TahunAkademik tahun, Prodi prodi);

    TahunAkademikProdi findByStatusAndProdi(StatusRecord statusRecord, Prodi prodi);

    Iterable<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikDesc(List<StatusRecord> hapus);

    TahunAkademikProdi findByTahunAkademikAndStatusAndProdi(TahunAkademik tahunAkademik, StatusRecord statusRecord,
            Prodi prodi);

    Page<TahunAkademikProdi> findByStatusOrderByTahunAkademikKodeTahunAkademikDesc(StatusRecord statusRecord,
            Pageable page);

    Page<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikKodeTahunAkademik(List<StatusRecord> statusRecords,
            Pageable page);

    Page<TahunAkademikProdi> findByTahunAkademikKodeTahunAkademikContainingOrderByTahunAkademikKodeTahunAkademikDesc(
            String ada, Pageable page);

    @Modifying
    @Query(value = "update tahun_akademik_prodi set status = 'NONAKTIF' where id <> ?1 and id_prodi = ?2 and status = 'AKTIF'", nativeQuery = true)
    int updateDataTahunAkademikProdi(String idTahunAkademikProdi, String idProdi);

    @Modifying
    @Query("UPDATE TahunAkademikProdi c SET c.status = :status WHERE c.id = :tahunAKademikProdi and c.prodi = :prodi")
    int updateTahunAKademikProdi2(@Param("tahunAKademikProdi") String tahunAKademikProdi, @Param("prodi") Prodi prodi,
            @Param("status") StatusRecord statusRecord);

    List<TahunAkademikProdi> findByTahunAkademikAndProdiAndStatus(TahunAkademik tahunAkademik, Prodi prodi,
            StatusRecord statusRecord);

    @Query(value = "select a.id, c.nama_prodi, b.kode_tahun_akademik,  b.nama_tahun_akademik, b.tahun, a.status from tahun_akademik_prodi as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join prodi as c on c.id = a.id_prodi where b.status = 'AKTIF' order by c.nama_prodi asc", nativeQuery = true)
    List<Object[]> listTahaunAkademikProdi();

    @Query(value = "SELECT a.id, c.namaProdi, b.kodeTahunAkademik, b.namaTahunAkademik, b.tahun, a.status\n" + //
            "    FROM TahunAkademikProdi a\n" + //
            "    JOIN a.tahunAkademik b\n" + //
            "    JOIN a.prodi c\n" + //
            "    WHERE b = :tahunAkademik AND c = :prodi\n" + //
            "    ORDER BY c.namaProdi ASC")
    List<Object[]> tahunAkademikProdiGet(@Param("tahunAkademik") TahunAkademik tahunAkademik,
            @Param("prodi") Prodi prodi);

    TahunAkademikProdi findByTahunAkademikAndProdi(TahunAkademik tahunAkademik, Prodi idProdi);
}
