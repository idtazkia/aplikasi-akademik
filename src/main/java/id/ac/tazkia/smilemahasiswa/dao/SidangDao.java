package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SidangDao extends PagingAndSortingRepository<Sidang, String> {
    Sidang findBySeminarAndAkademikAndJamMulaiNotNullAndJamMulaiNotNull(Seminar seminar,StatusApprove statusAppr);
    List<Sidang> findBySeminarOrderByAkademikDescTanggalInputDesc(Seminar seminar);
    List<Sidang> findBySeminar(Seminar seminar);
    Page<Sidang> findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikNotInAndStatusSidangNotIn(TahunAkademik tahunAkademik, Prodi prodi, List<StatusApprove> statusApproves,List<StatusApprove> statusSidang, Pageable page);
    Page<Sidang> findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikAndStatusSidangNotIn(TahunAkademik tahunAkademik, Prodi prodi, StatusApprove statusApproves,List<StatusApprove> statusApprove,Pageable page);
    Page<Sidang> findByTahunAkademikAndSeminarNoteMahasiswaIdProdiAndAkademikAndStatusSidangAndJamMulaiNotNullAndRuanganNotNull(TahunAkademik tahunAkademik, Prodi prodi, StatusApprove statusApproves,StatusApprove statusApprove, Pageable page);
    Page<Sidang> findByTahunAkademikAndAkademikAndStatusSidangAndJamMulaiNotNullAndRuanganNotNull(TahunAkademik tahunAkademik, StatusApprove statusApproves,StatusApprove statusApprove, Pageable page);
    @Query(value = "select 'Jadwal_kuliah' as jenis, c.nama_matakuliah as keterangan, e.nama_karyawan as milik  from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id inner join dosen as d on a.id_dosen_pengampu=d.id inner join karyawan as e on d.id_karyawan=e.id where id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF' and jam_mulai >= ?4 and jam_mulai <= ?5  or id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF'  and jam_selesai >= ?4 and jam_selesai <= ?5 union select 'Seminar' as jenis, b.judul as keterangan, c.nama as milik from seminar as a inner join note as b on a.id_note=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where a.status='APPROVED' and tanggal_ujian=?6 and jam_mulai >= ?4 and jam_mulai <= ?5 and ruangan = ?3 or a.status='APPROVED' and tanggal_ujian=?6 and jam_selesai >= ?4 and jam_selesai <= ?5 and ruangan = ?3 union select 'Sidang' as jenis, s.judul_tugas_akhir as keterangan, c.nama as milik from sidang as s inner join seminar as a on s.id_seminar=a.id inner join note as b on a.id_note=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where s.akademik='APPROVED' and s.tanggal_ujian=?6 and s.jam_mulai >= ?4 and s.jam_mulai <= ?5 and s.ruangan = '1.6' or s.akademik='APPROVED' and s.tanggal_ujian=?6 and s.jam_selesai >= ?4 and s.jam_selesai <= ?5 and s.ruangan = ?3 limit ?7", nativeQuery = true)
    Object[] validasiJadwalSidang(TahunAkademik tahunAkademik, Hari hari, Ruangan ruangan, LocalTime jamMulai, LocalTime jamSelesai, LocalDate tanggalUjian, Integer limit);
    Sidang findBySeminarAndAkademik(Seminar seminar, StatusApprove waiting);

    @Query(value = "select * from sidang where id = ?1 and (ka = ?2 or kb = ?2 or kc = ?2 or kd = ?2 or ua = ?2 or ub = ?2 or uc = ?2 or ud = ?2 or pa = ?2 or pb = ?2 or pc = ?2 or pd = ?2)", nativeQuery = true)
    Object validasiPublishNilai(Sidang sidang, BigDecimal nilai);

    @Query("select s from Sidang s where s.tahunAkademik = ?1 and s.akademik = ?2 and s.statusSidang = ?2 and (s.ketuaPenguji = ?3 or s.pembimbing = ?3 or s.dosenPenguji = ?3)")
    List<Sidang> listDosenSidang(TahunAkademik tahunAkademik,StatusApprove statusApprove,Dosen dosen);

    @Query(value = "SELECT s.* FROM sidang as s inner join seminar as se on s.id_seminar = se.id inner join note as n on se.id_note = n.id where n.id_mahasiswa = ?1 and s.status_sidang not in ('HAPUS')", nativeQuery = true)
    List<Object> cekSidang(Mahasiswa mahasiswa);

}
