package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteDao extends PagingAndSortingRepository <Note, String> {
    Note findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusApprove waiting);

    List<Note> findByMahasiswa(Mahasiswa mahasiswa);

    @Query("select n from Note n where n.tahunAkademik = :tahun and n.mahasiswa = :mahasiswa and n.status not in (:status)")
    Note cariKonsepNot(@Param("tahun") TahunAkademik tahunAkademik,@Param("mahasiswa")Mahasiswa mahasiswa,@Param("status") StatusApprove statusApprove);

    List<Note> findByMahasiswaOrderByTanggalInputDesc(Mahasiswa mahasiswa);

    Page<Note> findByTahunAkademikAndMahasiswaIdProdiAndStatus(TahunAkademik tahun, Prodi prodi, StatusApprove status, Pageable pageable);

    Page<Note> findByTahunAkademikAndMahasiswaIdProdiAndStatusNotIn(TahunAkademik tahun, Prodi prodi, List<StatusApprove> asList, Pageable pageable);

    @Query("select n from Note n where n.status = 'APPROVED' and n.dosen = :dosen or n.dosen2 = :dosen and  n.status = 'APPROVED'")
    List<Note> cariDosenPembimbing(@Param("dosen")Dosen dosen);
}
