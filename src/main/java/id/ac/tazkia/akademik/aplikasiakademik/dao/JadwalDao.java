package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal,String>
{
    List<Jadwal>findByStatusNotIn(StatusRecord statusRecord);
    List<Jadwal>findByTahunAkademikAndIdHariNotNull(TahunAkademik tahunAkademikProdi);
    List<Jadwal>findByTahunAkademikProdiAndIdKelasAndAksesAndStatusAndIdHariNotNull(TahunAkademikProdi tahunAkademikProdi,Kelas kelas,Akses akses, StatusRecord statusRecord);
    List<Jadwal> findByTahunAkademikAndIdKelasAndStatusAndIdHariNotNull(TahunAkademik tahunAkademik,Kelas kelas,StatusRecord statusRecord);
    List<Jadwal>findByTahunAkademikAndAksesAndStatusAndIdHariNotNull(TahunAkademik tahunAkademik,Akses akses, StatusRecord statusRecord);
    List<Jadwal>findByTahunAkademikAndProdiAndAksesAndStatusAndIdHariNotNull(TahunAkademik tahunAkademik,Prodi prodi,Akses akses, StatusRecord statusRecord);
    Page<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdi(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Pageable page);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariAndProgram(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Hari hari,Program program);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, String hari,Program program);
    List<Jadwal> findByStatusNotInAndIdHariIdAndDosenAndTahunAkademikAndIdHariNotNull(StatusRecord statusRecord,String hari,Dosen dosen,TahunAkademik tahunAkademik);
    Iterable<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariNullAndJamMulaiNullAndJamSelesaiNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi);

    @Query("select j from Jadwal j where j.dosen = :dosen and j.id not in (:id) and j.tahunAkademikProdi = :tahun and j.idHari = :hari and j.ruangan = :ruangan and (j.jamMulai between :mulai and :selesai or j.jamSelesai between :mulai and :selesai or :jamInput between j.jamMulai and j.jamSelesai)")
    List<Jadwal> cariJadwal(@Param("dosen") Dosen dosen,@Param("id") String id, @Param("tahun")TahunAkademikProdi t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("mulai")LocalTime mulai, @Param("selesai")LocalTime selesai, @Param("jamInput") LocalTime jamInput);

    @Query("select distinct j.dosen from Jadwal j where j.status =:status and j.tahunAkademik = :tahunAktif")
    Page<Jadwal> cariDosen(@Param("status") StatusRecord status,@Param("tahunAktif")TahunAkademik tahunAkademik,Pageable page);

    List<Jadwal>findByStatusAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);
    List<Jadwal>findByStatusAndTahunAkademikAndDosenAndIdHariNotNull(StatusRecord statusRecord,TahunAkademik tahunAkademik,Dosen dosen);

    List<Jadwal> findByStatusAndTahunAkademikAndProdiAndProgram(StatusRecord status, TahunAkademik tahunAkademik,Prodi prodi,Program program);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.idHari = :hari and j.ruangan = :ruangan")
    List<Jadwal> cariSesi(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.idHari = :hari and j.idKelas = :kelas")
    List<Jadwal> cariKelas(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("kelas")Kelas kelas);

    @Query("select j.sesi from Jadwal j where j.tahunAkademik = :tahun and j.idHari = :hari and j.dosen = :dosen")
    List<Jadwal> validasiDosen(@Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("dosen")Dosen dosen);

    List<Jadwal> findByStatusAndTahunAkademikAndRuanganAndIdHariAndSesi(StatusRecord statusRecord, TahunAkademik tahunAkademik, Ruangan ruangan, Hari hari, String s);


}

