package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Angkatan;
import id.ac.tazkia.smilemahasiswa.entity.Dokumen;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DokumenDao extends PagingAndSortingRepository<Dokumen, String>, CrudRepository<Dokumen, String> {

    Page<Dokumen> findByStatusOrderByStartDateAsc(StatusRecord status, Pageable pageable);

    Page<Dokumen> findByStatusAndDeskripsiContainingIgnoreCaseAndNamaFileContainingIgnoreCaseOrderByStartDateAsc(StatusRecord status, String deskripsi, String namaFile, Pageable pageable);

    Page<Dokumen> findByStatusAndDeskripsiContainingIgnoreCaseOrStatusAndNamaFileContainingIgnoreCaseOrderByStartDateAsc(StatusRecord status, String deskripsi, StatusRecord statusRecord2, String namaFile, Pageable pageable);

    List<Dokumen> findByStatusAndStartDateLessThanEqualAndAngkatansAndProdisAndEndDateGreaterThanEqualOrderByTanggalInsertDesc(StatusRecord statusRecord, LocalDate localDate, Angkatan angkatan, Prodi prodi, LocalDate localDate2);
}
