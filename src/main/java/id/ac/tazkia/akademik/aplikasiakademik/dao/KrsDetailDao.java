package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    Page<KrsDetail> findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(Krs krs, Mahasiswa mahasiswa, Pageable page);
    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.idHari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.idHari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    KrsDetail findByJadwalAndMahasiswaAndKrsAndStatus(Jadwal jadwal,Mahasiswa mahasiswa,Krs krs,StatusRecord statusRecord);
    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);
    KrsDetail findByJadwalAndStatusAndMahasiswa(Jadwal jadwal,StatusRecord statusRecord,Mahasiswa mahasiswa);
    KrsDetail findByJadwalAndStatusAndMahasiswaAndKrsTahunAkademikNotIn(Jadwal jadwal,StatusRecord statusRecord,Mahasiswa mahasiswa,TahunAkademik tahunAkademik);

    List<KrsDetail> findByKrsAndStatus(Krs k,StatusRecord statusRecord);
    KrsDetail findByMatakuliahKurikulumAndGradeNotIn(MatakuliahKurikulum matakuliahKurikulum, List<Grade> grades);
    KrsDetail findByMatakuliahKurikulumAndMahasiswaAndStatus(MatakuliahKurikulum matakuliahKurikulum,Mahasiswa mahasiswa,StatusRecord statusRecorda);
    KrsDetail findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsTahunAkademikNotIn(MatakuliahKurikulum matakuliahKurikulum,Mahasiswa mahasiswa,StatusRecord statusRecorda,TahunAkademik tahunAkademik);
}