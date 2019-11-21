package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    List<KrsDetail> findByStatusAndKrsAndMahasiswaOrderByJadwalHariAscJadwalJamMulaiAsc(StatusRecord status,Krs krs, Mahasiswa mahasiswa);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapPresensi (kd.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah,kd.jadwal.matakuliahKurikulum.jumlahSks,kd.jadwal.kelas.namaKelas,kd.jadwal.dosen.karyawan.namaKaryawan,0,kd.jadwal.id)from KrsDetail kd where kd.status = :status and kd.krs =:krs and kd.mahasiswa = :mahasiswa order by kd.jadwal.hari asc,kd.jadwal.jamMulai asc ")
    List<RekapPresensi> cariPresensi(@Param("status")StatusRecord statusRecord,@Param("krs")Krs krs,@Param("mahasiswa")Mahasiswa mahasiswa);

    @Query("select kd.id from KrsDetail kd where kd.status = :status and kd.krs =:krs and kd.mahasiswa = :mahasiswa order by kd.jadwal.hari asc,kd.jadwal.jamMulai asc ")
    List<String> cariJadwalId(@Param("status")StatusRecord statusRecord,@Param("krs")Krs krs,@Param("mahasiswa")Mahasiswa mahasiswa);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.hari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.hari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

    Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);

    List<KrsDetail> findByJadwalAndStatusOrderByMahasiswaNamaAsc(Jadwal jadwal,StatusRecord statusRecord);

    List<KrsDetail> findByMatakuliahKurikulumAndStatusAndMahasiswa(MatakuliahKurikulum matakuliahKurikulum,StatusRecord statusRecord,Mahasiswa mahasiswa);
    List<KrsDetail> findByMatakuliahKurikulumMatakuliahKodeMatakuliahAndMahasiswaAndStatusAndKrsNotIn(String kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,Krs krs);
    List<KrsDetail> findByMatakuliahKurikulumAndMahasiswaAndStatusAndNilaiAkhirGreaterThan(MatakuliahKurikulum kode,Mahasiswa mahasiswa,StatusRecord statusRecorda,BigDecimal bigDecimal);
    List<KrsDetail> findByJadwalAndStatusAndKrsTahunAkademik(Jadwal jadwal, StatusRecord statusRecord, TahunAkademik tahunAkademik);

    Page<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord,Pageable page);
    List<KrsDetail> findByJadwalAndStatus(Jadwal jadwal,StatusRecord statusRecord);
    KrsDetail findByJadwalAndStatusAndKrs(Jadwal jadwal,StatusRecord statusRecord,Krs krs);

    List<KrsDetail> findByMahasiswaAndKrsTahunAkademikAndStatus(Mahasiswa mahasiswa,TahunAkademik tahunAkademik,StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.TestDto(kd.id,kd.mahasiswa.nim,kd.mahasiswa.nama,0,kd.nilaiUts,kd.nilaiUas,0) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    List<TestDto> penilaianList(@Param("jadwal")String jadwal,@Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.NilaiDto(kd.mahasiswa.nim,kd.nilaiUts) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    List<NilaiDto> nilaiDto(@Param("jadwal")String jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks,kd.matakuliahKurikulum.statusSkripsi) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status")
    Page<RekapSksDto> cariSks(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapSksDto(kd.krs.id,kd.mahasiswa.nim,kd.mahasiswa.nama,kd.matakuliahKurikulum.jumlahSks,kd.matakuliahKurikulum.statusSkripsi) from KrsDetail kd where kd.krs.tahunAkademik = :tahun and kd.mahasiswa.idProdi = :prodi and kd.status = :status and kd.matakuliahKurikulum.statusSkripsi = :skripsi")
    Page<RekapSksDto> tanpaSkripsi(@Param("tahun")TahunAkademik tahunAkademik, @Param("prodi")Prodi prodi, @Param("status")StatusRecord statusRecord,@Param("skripsi")StatusRecord skripsi, Pageable page);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.AbsenDto (kd.mahasiswa.nim,kd.mahasiswa.nama,kd.kodeUts,1,0) from KrsDetail kd where kd.jadwal = :jadwal and kd.status = :status")
    Iterable<AbsenDto> cariId (@Param("jadwal")Jadwal jadwal, @Param("status")StatusRecord statusRecord);

    @Query("select count (kd.id) from KrsDetail kd where kd.jadwal.id = :jadwal and kd.status = :status")
    Long jumlahMahasiswa(@Param("jadwal")String jadwal,@Param("status")StatusRecord statusRecord);

}