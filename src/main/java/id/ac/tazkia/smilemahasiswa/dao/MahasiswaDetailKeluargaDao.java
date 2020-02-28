package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaDetailKeluarga;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MahasiswaDetailKeluargaDao extends PagingAndSortingRepository<MahasiswaDetailKeluarga, String> {
    MahasiswaDetailKeluarga findByMahasiswa(Mahasiswa mahasiswa);
    Page<MahasiswaDetailKeluarga> findByMahasiswaStatusNotInAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByMahasiswaNama(List<StatusRecord> statusRecord, String status, String nim, Pageable page);

    Page<MahasiswaDetailKeluarga> findByMahasiswaStatusNotIn(List<StatusRecord> hapus, Pageable page);
}
