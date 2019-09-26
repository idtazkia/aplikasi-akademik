package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiMahasiswaDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    Page<KrsDetail> findByKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(Krs krs, Mahasiswa mahasiswa, Pageable page);
    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.hari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.hari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);

    List<KrsDetail> findByMatakuliahKurikulumAndStatusAndMahasiswa(MatakuliahKurikulum matakuliahKurikulum,StatusRecord statusRecord,Mahasiswa mahasiswa);
    List<KrsDetail> findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(String kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    KrsDetail findByMatakuliahKurikulumOrMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(MatakuliahKurikulum matakuliahKurikulum,String kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal jadwal, StatusRecord statusRecord, TahunAkademik tahunAkademik);
    Page<KrsDetail> findByKrs(List<Krs> krs,Pageable page);
    Page<KrsDetail> findDistinctByKrsTahunAkademik(TahunAkademik tahunAkademik,Pageable page);

    @Query("select k.mahasiswa.id from KrsDetail k where k.status = :status and k.krs= :krs")
    List<KrsDetail> cariKrs(@Param("status")StatusRecord status, @Param("krs")List<Krs> krs);

    Page<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord,Pageable page);

    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatus(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status")
    Page<RekapSksDto> cariSks(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status and  kd.matakuliahKurikulum.statusSkripsi not in (:skripsi)")
    Page<RekapSksDto> tanpaSkripsi(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord,@Param("skripsi")StatusRecord status, Pageable page);



}