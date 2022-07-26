package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.TahunAkademikIntDto;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface TahunAkademikDao extends PagingAndSortingRepository<TahunAkademik, String> {


    TahunAkademik findByStatus(StatusRecord aktif);

    TahunAkademik findByStatusNotInAndKodeTahunAkademik(List<StatusRecord> status, String kode);

    TahunAkademik findByStatusAndId(StatusRecord statusRecord, String id);

    TahunAkademik findByStatusNotAndId(StatusRecord statusRecord, String id);

    TahunAkademik findByKodeTahunAkademik(String kode);

    TahunAkademik findByKodeTahunAkademikAndJenis(String kode,StatusRecord statusRecord);

    TahunAkademik findByNamaTahunAkademikAndStatus(String nama, StatusRecord statusRecord);

    TahunAkademik findByStatusAndJenis(StatusRecord statusRecord, StatusRecord jenis);

    List<TahunAkademik> findByStatusOrderByKodeTahunAkademikDesc(StatusRecord statusRecord);

    Iterable<TahunAkademik> findByStatusNotInOrderByTahunDesc(List<StatusRecord> statusRecords);

    @Query(value = "select tahun.id,tahun.nama_tahun_akademik from tahun_akademik as tahun where status not in(?1) order by nama_tahun_akademik desc", nativeQuery = true)
    List<Object[]> cariTahunAkademik(List<StatusRecord> statusRecord);

    Page<TahunAkademik> findByStatusInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(List<StatusRecord> status, String search, Pageable page);

    @Query("select ta from TahunAkademik ta where ta.status not in (:status) order by ta.kodeTahunAkademik desc")
    Page<TahunAkademik> cariTahunAkademik(@Param("status") StatusRecord hapus, Pageable page);

    List<TahunAkademik> findByStatusNotInOrderByNamaTahunAkademikDesc(List<StatusRecord> asList);


    @Query(value = "select id as idTahunAkademik, kode_tahun_akademik as kodeTahunAkademik, nama_tahun_akademik as namaTahunAkademik, tanggal_mulai as tanggalMulai, tanggal_selesai as tanggalSelesai,\n" +
            "tanggal_mulai_krs as tanggalMulaiKrs, tanggal_selesai_krs as tanggalSelesaiKrs, tanggal_mulai_kuliah as tanggalMulaiKuliah, tanggal_selesai_kuliah as tanggalSelesaiKuliah,\n" +
            "tanggal_mulai_uts as tanggalMulaiUts, tanggal_selesai_uts as tanggalSelesaiUts, tanggal_mulai_uas as tanggalMulaiUas, tanggal_selesai_uas as tanggalSelesaiUas,\n" +
            "tanggal_mulai_nilai as tanggalMulaiNilai, tanggal_selesai_nilai as tanggalSelesaiNilai, tahun as tahun, status as status, jenis as jenis\n" +
            "from tahun_akademik \n" +
            "order by kode_tahun_akademik desc", nativeQuery = true)
    List<TahunAkademikIntDto> apiTahunAkademik();

    TahunAkademik findTopByStatusNotInOrderByKodeTahunAkademikDesc(List<StatusRecord> asList);

}
