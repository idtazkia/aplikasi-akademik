package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface NoteDao extends PagingAndSortingRepository <Note,String> {
    Note findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusApprove statusRecord);
    List<Note> findByMahasiswa(Mahasiswa mahasiswa);
    List<Note> findByMahasiswaOrderByTanggalInputDesc(Mahasiswa mahasiswa);
    Page<Note> findByTahunAkademikAndMahasiswaIdProdiAndStatusNotIn(TahunAkademik tahunAkademik, Prodi prodi, StatusApprove statusApprove, Pageable pageable);
    Page<Note> findByTahunAkademikAndMahasiswaIdProdiAndStatus(TahunAkademik tahunAkademik, Prodi prodi, StatusApprove statusApprove, Pageable pageable);
    Note findByTahunAkademikAndMahasiswaAndStatusNotIn(TahunAkademik tahunAkademik,Mahasiswa mahasiswa,StatusApprove statusApprove);
    List<Note> findByStatusAndMahasiswa(StatusApprove statusApprove, Mahasiswa mahasiswa);
}
