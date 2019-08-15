package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    Page<KrsDetail> findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(Krs krs, Mahasiswa mahasiswa, Pageable page);
    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.idHari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.idHari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);

    KrsDetail findByMatakuliahKurikulumAndMahasiswaAndStatus(MatakuliahKurikulum matakuliahKurikulum,Mahasiswa mahasiswa,StatusRecord statusRecorda);
    List<KrsDetail> findByMatakuliahKurikulumAndStatusAndMahasiswa(MatakuliahKurikulum matakuliahKurikulum,StatusRecord statusRecord,Mahasiswa mahasiswa);
//    KrsDetail findByMatakuliahKurikulumAndMahasiswaAndStatusAndKrsNotIn(MatakuliahKurikulum matakuliahKurikulum,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    KrsDetail findByMatakuliahKurikulumOrMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(MatakuliahKurikulum matakuliahKurikulum,String kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal jadwal, StatusRecord statusRecord, TahunAkademik tahunAkademik);
    Page<KrsDetail> findByKrs(List<Krs> krs,Pageable page);
    Page<KrsDetail> findDistinctByKrsTahunAkademik(TahunAkademik tahunAkademik,Pageable page);

    @Query("select k.mahasiswa.id from KrsDetail k where k.status = :status and k.krs= :krs")
    List<KrsDetail> cariKrs(@Param("status")StatusRecord status, @Param("krs")List<Krs> krs);

    Page<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord,Pageable page);


}