package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Beasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaBeasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MahasiswaBeasiswaDao extends PagingAndSortingRepository<MahasiswaBeasiswa, String> {
    List<MahasiswaBeasiswa> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);
    List<MahasiswaBeasiswa> findByMahasiswaAndBeasiswaAndStatus(Mahasiswa mahasiswa,Beasiswa beasiswa, StatusRecord statusRecord);
}