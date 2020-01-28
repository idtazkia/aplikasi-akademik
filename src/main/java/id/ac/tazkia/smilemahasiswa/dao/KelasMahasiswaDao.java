package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.KelasMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KelasMahasiswaDao extends PagingAndSortingRepository<KelasMahasiswa, String> {
    KelasMahasiswa findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord aktif);

    Iterable<KelasMahasiswa> findByKelasAndStatus(Kelas kelas, StatusRecord aktif);

    KelasMahasiswa findByMahasiswaAndKelas(Mahasiswa mahasiswa, Kelas kelas);
}
