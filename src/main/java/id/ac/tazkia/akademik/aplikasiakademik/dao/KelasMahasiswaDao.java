package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.KelasMahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KelasMahasiswaDao extends PagingAndSortingRepository <KelasMahasiswa, String> {
    KelasMahasiswa findByMahasiswaAndKelas(Mahasiswa mahasiswa, Kelas kelas);
    Iterable<KelasMahasiswa> findByKelasAndStatus(Kelas kelas, StatusRecord statusRecord    );
    KelasMahasiswa findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);
    KelasMahasiswa findFirstByKelasAndStatus(Kelas kelas, StatusRecord statusRecord);


}
