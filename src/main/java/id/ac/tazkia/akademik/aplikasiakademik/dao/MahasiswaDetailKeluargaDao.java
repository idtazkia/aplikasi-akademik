package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MahasiswaDetailKeluarga;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MahasiswaDetailKeluargaDao extends PagingAndSortingRepository<MahasiswaDetailKeluarga, String> {
    MahasiswaDetailKeluarga findByMahasiswa(Mahasiswa mahasiswa);
    Page<MahasiswaDetailKeluarga> findByMahasiswaStatusNotInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNama(StatusRecord statusRecord, String status, String nim, Pageable page);

    Page<MahasiswaDetailKeluarga> findByMahasiswaStatusNotIn(StatusRecord hapus, Pageable page);
}
