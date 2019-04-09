package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
   Page<KrsDetail> findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(Krs krs, Mahasiswa mahasiswa, Pageable page);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.idHari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.idHari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

   Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    KrsDetail findByJadwalAndMahasiswaAndKrs(Jadwal jadwal,Mahasiswa mahasiswa,Krs krs);
    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);
    KrsDetail findByIdAndMahasiswa(String id, Mahasiswa mahasiswa);

}
