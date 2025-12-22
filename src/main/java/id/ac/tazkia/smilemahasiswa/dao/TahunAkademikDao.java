package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.tahunakademik.TahunAkademikIntDto;
import id.ac.tazkia.smilemahasiswa.dto.tlc.periode.PeriodeDto;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TahunAkademikDao extends PagingAndSortingRepository<TahunAkademik, String> {

        List<TahunAkademik> findAllByOrderByStatusAscKodeTahunAkademikDesc();

        List<TahunAkademik> findByIdIn(List<String> ids);

        TahunAkademik findByStatus(StatusRecord aktif);

        TahunAkademik findByStatusNotInAndKodeTahunAkademik(List<StatusRecord> status, String kode);

        TahunAkademik findByStatusAndId(StatusRecord statusRecord, String id);

        TahunAkademik findByStatusNotAndId(StatusRecord statusRecord, String id);

        TahunAkademik findByKodeTahunAkademik(String kode);

        TahunAkademik findByKodeTahunAkademikAndJenis(String kode, StatusRecord statusRecord);

        TahunAkademik findByNamaTahunAkademikAndStatus(String nama, StatusRecord statusRecord);

        TahunAkademik findByStatusAndJenis(StatusRecord statusRecord, StatusRecord jenis);

        List<TahunAkademik> findByStatusOrderByKodeTahunAkademikDesc(StatusRecord statusRecord);

        List<TahunAkademik> findByStatusNotInOrderByKodeTahunAkademikDesc(List<StatusRecord> statusRecords);

        Iterable<TahunAkademik> findByStatusNotInOrderByTahunDesc(List<StatusRecord> statusRecords);

        @Query(value = "select tahun.id,tahun.nama_tahun_akademik from tahun_akademik as tahun where status not in(?1) order by nama_tahun_akademik desc", nativeQuery = true)
        List<Object[]> cariTahunAkademik(List<StatusRecord> statusRecord);

        Page<TahunAkademik> findByStatusInAndNamaTahunAkademikContainingIgnoreCaseOrderByKodeTahunAkademikDesc(
                        List<StatusRecord> status, String search, Pageable page);

        @Query("select ta from TahunAkademik ta where ta.status not in (:status) order by ta.kodeTahunAkademik desc")
        Page<TahunAkademik> cariTahunAkademik(@Param("status") StatusRecord hapus, Pageable page);

        List<TahunAkademik> findByStatusNotInOrderByNamaTahunAkademikDesc(List<StatusRecord> asList);

        @Query(value = "select id as idTahunAkademik, kode_tahun_akademik as kodeTahunAkademik, nama_tahun_akademik as namaTahunAkademik, tanggal_mulai as tanggalMulai, tanggal_selesai as tanggalSelesai,\n"
                        +
                        "tanggal_mulai_krs as tanggalMulaiKrs, tanggal_selesai_krs as tanggalSelesaiKrs, tanggal_mulai_kuliah as tanggalMulaiKuliah, tanggal_selesai_kuliah as tanggalSelesaiKuliah,\n"
                        +
                        "tanggal_mulai_uts as tanggalMulaiUts, tanggal_selesai_uts as tanggalSelesaiUts, tanggal_mulai_uas as tanggalMulaiUas, tanggal_selesai_uas as tanggalSelesaiUas,\n"
                        +
                        "tanggal_mulai_nilai as tanggalMulaiNilai, tanggal_selesai_nilai as tanggalSelesaiNilai, tahun as tahun, status as status, jenis as jenis\n"
                        +
                        "from tahun_akademik \n" +
                        "order by kode_tahun_akademik desc", nativeQuery = true)
        List<TahunAkademikIntDto> apiTahunAkademik();

        TahunAkademik findTopByStatusNotInOrderByKodeTahunAkademikDesc(List<StatusRecord> asList);

        TahunAkademik findByStatusAndJenisNot(StatusRecord status, StatusRecord jenis);

        @Query(value = "select ta.* from mahasiswa m inner join krs k on m.id=k.id_mahasiswa inner join tahun_akademik ta on k.id_tahun_akademik=ta.id where id_mahasiswa=?1 and k.status='AKTIF' and ta.jenis <> 'PENDEK' order by ta.kode_tahun_akademik asc limit 1", nativeQuery = true)
        TahunAkademik tahunAkademikAwal(String idMahasiswa);

        @Query(value = "select ta.* from mahasiswa m inner join krs k on m.id=k.id_mahasiswa inner join tahun_akademik ta on k.id_tahun_akademik=ta.id where id_mahasiswa=?1 and k.status='AKTIF' and ta.jenis <> 'PENDEK' order by ta.kode_tahun_akademik desc limit 1", nativeQuery = true)
        TahunAkademik tahunAkademikAkhir(String idMahasiswa);

        Integer countByKodeTahunAkademikGreaterThanEqualAndKodeTahunAkademikLessThanEqualAndStatusNotInAndJenisNotIn(
                        String kodeAwal, String kodeTahunTerakhir, List<StatusRecord> status,
                        List<StatusRecord> notJenis);

        @Query(value = "SELECT id,CONCAT(nama_tahun_akademik, ' s/d ', LEFT(nama_tahun_akademik, LENGTH(nama_tahun_akademik) - 6), ' Genap') AS tahun FROM tahun_akademik WHERE nama_tahun_akademik like %?1% and status NOT IN ('HAPUS') AND jenis NOT IN ('GENAP', 'PENDEK') ORDER BY kode_tahun_akademik DESC limit ?2 offset ?3", nativeQuery = true)
        List<PeriodeDto> listTahunAkademikSearch(String search, Integer limit, Integer offset);

        @Query(value = "SELECT id,CONCAT(nama_tahun_akademik, ' s/d ', LEFT(nama_tahun_akademik, LENGTH(nama_tahun_akademik) - 6), ' Genap') AS tahun FROM tahun_akademik WHERE status NOT IN ('HAPUS') AND jenis NOT IN ('GENAP', 'PENDEK') ORDER BY kode_tahun_akademik DESC limit ?1 offset ?2", nativeQuery = true)
        List<PeriodeDto> listTahunAkademik(Integer limit, Integer offset);

        @Query(value = "SELECT id,CONCAT(nama_tahun_akademik, ' s/d ', LEFT(nama_tahun_akademik, LENGTH(nama_tahun_akademik) - 6), ' Genap') AS tahun FROM tahun_akademik WHERE status NOT IN ('HAPUS') AND jenis NOT IN ('GENAP', 'PENDEK') ORDER BY kode_tahun_akademik DESC", nativeQuery = true)
        List<PeriodeDto> listSemuaTahunAkademik();

        @Query(value = "SELECT count(*) FROM tahun_akademik WHERE status NOT IN ('HAPUS') AND jenis NOT IN ('GENAP', 'PENDEK') ORDER BY kode_tahun_akademik DESC", nativeQuery = true)
        Integer countTahunAkademik();

        TahunAkademik findFirstByKodeTahunAkademikLessThanAndStatusNotInOrderByKodeTahunAkademikDesc(String kode, List<StatusRecord> status);
}
