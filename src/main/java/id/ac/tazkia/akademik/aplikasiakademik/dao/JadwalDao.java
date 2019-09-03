package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal,String> {

    static final String QUERY_JADWAL_DOSEN_DTO =
            "select new id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto(" +
            "j.dosen.id,j.dosen.absen, j.dosen.karyawan.namaKaryawan, j.dosen.karyawan.rfid, " +
            "j.id, j.jamMulai, j.jamSelesai, 1) " +
            "from Jadwal j where j.ruangan = :ruangan " +
            "and j.tahunAkademik = :tahunAkademik " +
            "and j.hari = :hari " +
            "and j.jamMulai between :mulai and :sampai ";

    List<Jadwal>findByStatusNotIn(StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndHariNotNull(TahunAkademik tahunAkademik);
    List<Jadwal> findByTahunAkademikAndKelasAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Kelas kelas, StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndAksesAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Akses akses, StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndProdiAndAksesAndStatusAndHariNotNull(TahunAkademik tahunAkademik, Prodi prodi, Akses akses, StatusRecord statusRecord);
    List<Jadwal> findByStatusNotInAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariAndProgramAndHariNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Hari hari, Program program);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, String hari, Program program);
    Iterable<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndHariNullAndJamMulaiNullAndJamSelesaiNullAndKelasNotNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi);
    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses, '', '')from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademikProdi = :tahun order by j.kelas.namaKelas asc ")
    List<Jadwal> ploting(@Param("prodi") Prodi prodi,@Param("id") StatusRecord statusRecord, @Param("tahun")TahunAkademikProdi t);

    @Query("select j from Jadwal j where j.id not in (:id) and j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan and j.sesi= :sesi and j.status= :status")
    List<Jadwal> cariJadwal(@Param("id") String id, @Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("sesi")String sesi,@Param("status")StatusRecord statusRecord);

    @Query("select distinct j.dosen from Jadwal j where j.status =:status and j.tahunAkademik = :tahunAktif")
    Page<Jadwal> cariDosen(@Param("status") StatusRecord status,@Param("tahunAktif")TahunAkademik tahunAkademik,Pageable page);

    List<Jadwal>findByStatusAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);
    List<Jadwal> findByStatusAndTahunAkademikAndDosenAndHariNotNull(StatusRecord statusRecord, TahunAkademik tahunAkademik, Dosen dosen);

    Page<Jadwal> findByStatusAndTahunAkademikAndProdiAndProgramAndKelasNotNullAndHariNotNullOrderByDosenKaryawanNamaKaryawanAsc(StatusRecord status, TahunAkademik tahunAkademik,Prodi prodi,Program program,Pageable pageable);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan")
    List<Jadwal> cariSesi(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.kelas = :kelas")
    List<Jadwal> cariKelas(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("kelas")Kelas kelas);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.hari = :hari and j.dosen = :dosen")
    List<Jadwal> validasiDosen(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("dosen")Dosen dosen);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademikProdi = :tahun and j.hari= :hari and j.program= :program")
    List<Jadwal> schedule(@Param("prodi") Prodi prodi,@Param("id") StatusRecord statusRecord, @Param("tahun")TahunAkademikProdi t,@Param("hari")Hari hari,@Param("program")Program program);

    @Query(QUERY_JADWAL_DOSEN_DTO)
    JadwalDosenDto cariJadwalDosenPengampu(@Param("tahunAkademik") TahunAkademik ta, @Param("ruangan") Ruangan r, @Param("hari") Hari hari, @Param("mulai")LocalTime mulai, @Param("sampai") LocalTime sampai);

}

