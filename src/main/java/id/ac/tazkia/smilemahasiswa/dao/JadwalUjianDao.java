package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JadwalUjian;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface JadwalUjianDao extends PagingAndSortingRepository<JadwalUjian,String> {
    JadwalUjian findByJadwalAndStatusAndJenis(Jadwal jadwal, StatusRecord status, StatusRecord jenis);

    List<JadwalUjian> findByJadwalTahunAkademikAndStatusAndJenisOrderByTanggalAscJamMulaiAsc(TahunAkademik tahunAkademik, StatusRecord status, StatusRecord jenis);
    List<JadwalUjian> findByJadwalTahunAkademikAndStatusAndJenisAndTanggalOrderByJamMulaiAsc(TahunAkademik tahunAkademik, StatusRecord status, StatusRecord jenis,LocalDate tanggal);

    @Query(value = "SELECT distinct tanggal FROM jadwal_ujian as ju inner join jadwal as j on ju.id_jadwal = j.id where ju.status = 'AKTIF' and ju.jenis = ?1 and j.id_tahun_akademik = ?2 order by ju.tanggal asc;", nativeQuery = true)
    List<Date> listTanggal(String jenis, TahunAkademik tahunAkademik);
}
