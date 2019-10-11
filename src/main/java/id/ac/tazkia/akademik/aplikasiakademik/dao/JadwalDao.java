package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal,String> {
    List<Jadwal>findByStatusNotIn(StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndHariNotNull(TahunAkademik tahunAkademik);
    List<Jadwal> findByTahunAkademikAndKelasAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Kelas kelas, StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndAksesAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Akses akses, StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndProdiAndAksesAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Prodi prodi, Akses akses, StatusRecord statusRecord);
    List<Jadwal> findByStatusNotInAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariAndProgramAndHariNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Hari hari, Program program);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, String hari, Program program);
    Iterable<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariNullAndJamMulaiNullAndJamSelesaiNullAndKelasNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi);
    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses, j.ruangan.namaRuangan, j.hari.namaHari)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademikProdi = :tahun order by j.kelas.namaKelas asc ")
    List<PlotingDto> ploting(@Param("prodi") Prodi prodi,@Param("id") StatusRecord statusRecord, @Param("tahun")TahunAkademikProdi t);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses, '', '')from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademikProdi = :tahun and j.hari is null  and  j.jamMulai is null and j.kelas is not null order by j.kelas.namaKelas asc ")
    List<PlotingDto> plotingKosong(@Param("prodi") Prodi prodi,@Param("id") StatusRecord statusRecord, @Param("tahun")TahunAkademikProdi t);

    @Query("select j from Jadwal j where j.id not in (:id) and j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan and j.sesi= :sesi and j.status= :status")
    List<Jadwal> cariJadwal(@Param("id") String id, @Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("sesi")String sesi,@Param("status")StatusRecord statusRecord);

    @Query("select distinct j.dosen from Jadwal j where j.status =:status and j.tahunAkademik = :tahunAktif")
    Page<Jadwal> cariDosen(@Param("status") StatusRecord status,@Param("tahunAktif")TahunAkademik tahunAkademik,Pageable page);

    List<Jadwal>findByStatusAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);
    List<Jadwal> findByStatusAndTahunAkademikAndDosenAndHariNotNull(StatusRecord statusRecord, TahunAkademik tahunAkademik, Dosen dosen);

    Page<Jadwal> findByStatusAndTahunAkademikAndProdiAndProgramAndKelasNotNullAndHariNotNullOrderByDosenKaryawanNamaKaryawanAsc(StatusRecord status, TahunAkademik tahunAkademik,Prodi prodi,Program program,Pageable pageable);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan and j.status = :status")
    List<String> cariSesi(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r,@Param("status")StatusRecord s);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.kelas = :kelas and j.status = :status")
    List<String> cariKelas(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("kelas")Kelas kelas,@Param("status")StatusRecord s);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.dosen = :dosen and j.status = :status")
    List<String> validasiDosen(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("dosen")Dosen dosen,@Param("status")StatusRecord s);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademikProdi = :tahun and j.hari= :hari and j.program= :program")
    List<PlotingDto> schedule(@Param("prodi") Prodi prodi, @Param("id") StatusRecord statusRecord, @Param("tahun")TahunAkademikProdi t, @Param("hari")Hari hari, @Param("program")Program program);

    @Query("select j.kelas.id from  Jadwal j where j.tahunAkademik = :tahun and j.hari is null  and  j.jamMulai is null and j.kelas is not null and j.kelas is not null and j.status = :status")
    List<String> cariKrsKelas(@Param("tahun") TahunAkademik tahunAkademik,@Param("status")StatusRecord statusRecord);

    List<Jadwal> findByStatusAndTahunAkademikAndKelasAndHariNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,Kelas kelas);

    Page<Jadwal> findByStatusAndTahunAkademikAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,String nama,String matkul,Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUtsAndDosenKaryawanNamaKaryawanContainingIgnoreCaseOrMatakuliahKurikulumMatakuliahNamaMatakuliahContainingIgnoreCaseAndHariNotNullAndJamMulaiNotNullAndKelasNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,StatusApprove statusApprove,String nama,String matkul,Pageable page);

    Page<Jadwal> findByStatusAndTahunAkademikAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,Pageable pageable);
    Page<Jadwal> findByStatusAndTahunAkademikAndStatusUtsAndJamMulaiNotNullAndHariNotNullAndKelasNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,StatusApprove statusApprove,Pageable pageable);

}

