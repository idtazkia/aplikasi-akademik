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
    List<Jadwal>findByTahunAkademikProdi(TahunAkademikProdi tahunAkademikProdi);
    Page<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdi(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Pageable page);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariAndProgram(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Hari hari,Program program);
    List<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariIdAndProgram(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, String hari,Program program);
    Iterable<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdiAndIdHariNullAndJamMulaiNullAndJamSelesaiNull(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi);

    @Query("select j from Jadwal j where j.dosen = :dosen and j.id not in (:id) and j.tahunAkademikProdi = :tahun and j.idHari = :hari and j.ruangan = :ruangan and (j.jamMulai between :mulai and :selesai or j.jamSelesai between :mulai and :selesai or :jamInput between j.jamMulai and j.jamSelesai)")
    List<Jadwal> cariJadwal(@Param("dosen") Dosen dosen,@Param("id") String id, @Param("tahun")TahunAkademikProdi t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("mulai")LocalTime mulai, @Param("selesai")LocalTime selesai, @Param("jamInput") LocalTime jamInput);

    @Query("select distinct j.dosen from Jadwal j where j.tahunAkademik = :tahunAktif")
    Page<Jadwal> cariDosen(@Param("tahunAktif")TahunAkademik tahunAkademik,Pageable page);

    List<Jadwal>findByStatusAndTahunAkademik(StatusRecord statusRecord,TahunAkademik tahunAkademik);


}


