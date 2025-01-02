package id.ac.tazkia.smilemahasiswa.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.constant.MagangStatus;
import id.ac.tazkia.smilemahasiswa.entity.FileMagang;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

import java.util.List;

public interface FileMagangDao extends PagingAndSortingRepository<FileMagang, String> {
    List<FileMagang> findByMahasiswa(Mahasiswa mahasiswa);

    FileMagang findByStatusAndMahasiswa(StatusApprove status, Mahasiswa mahasiswa);

    List<FileMagang> findByMahasiswaAndStatusInOrderByStatusDesc(Mahasiswa mahasiswa, List<StatusApprove> asList);

    Page<FileMagang> findByMahasiswaIdProdiAndStatusInAndTahunAkademikOrderByStatusDescTanggalUploadAsc(Prodi prodi,
            List<StatusApprove> asList, TahunAkademik tahunAkademik,
            Pageable page);

    Page<FileMagang> findByMahasiswaIdProdiAndStatusInAndTahunAkademikAndJenisOrderByStatusDescTanggalUploadAsc(Prodi prodi,
            List<StatusApprove> asList, TahunAkademik tahunAkademik, MagangStatus magangStatus,
            Pageable page);

}
