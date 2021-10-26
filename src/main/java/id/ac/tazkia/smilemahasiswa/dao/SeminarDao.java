package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.graduation.RekapTugasAkhir;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SeminarDao extends PagingAndSortingRepository<Seminar, String> {
    Seminar findByNoteAndStatusNotIn(Note note, List<StatusApprove> statusRecords);
    Seminar findByNoteAndStatus(Note note, StatusApprove statusRecords);
    Page<Seminar> findByTahunAkademikAndNoteMahasiswaIdProdiAndStatusNotInOrderByStatusSemproDescPublishDesc(TahunAkademik tahunAkademik, Prodi prodi, List<StatusApprove> status, Pageable pageable);
    List<Seminar> findByTahunAkademikAndNoteMahasiswaIdProdiAndStatusNotInOrderByStatusDescTanggalInputDesc(TahunAkademik tahunAkademik, Prodi prodi, List<StatusApprove> status);
    @Query(value = "select 'Jadwal_kuliah' as jenis, c.nama_matakuliah as keterangan, e.nama_karyawan as milik  from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id inner join dosen as d on a.id_dosen_pengampu=d.id inner join karyawan as e on d.id_karyawan=e.id where id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF' and jam_mulai > ?4 and jam_mulai < ?5  or id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF'  and jam_selesai > ?4 and jam_selesai < ?5 union select 'Seminar' as jenis, b.judul as keterangan, c.nama as milik from seminar as a inner join note as b on a.id_note=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where a.status='APPROVED' and tanggal_ujian=?6 and jam_mulai > ?4 and jam_mulai < ?5 and ruangan = ?3 or a.status='APPROVED' and tanggal_ujian=?6 and jam_selesai > ?4 and jam_selesai < ?5 and ruangan = ?3 limit ?7", nativeQuery = true)
    Object[] validasiJadwalSeminar(TahunAkademik tahunAkademik, Hari hari, Ruangan ruangan, LocalTime jamMulai, LocalTime jamSelesai, LocalDate tanggalUjian, Integer limit);
    List<Seminar> findByNote(Note note);
    @Query("select s from Seminar s where s.status = 'APPROVED' and s.ketuaPenguji = :ketua or s.status = 'APPROVED' and s.dosenPenguji = :dosen or s.status = 'APPROVED' and s.note.dosen = :dosen1 and s.tahunAkademik = :tahun order by s.statusSempro desc ")
    List<Seminar> cariSeminar(@Param("ketua") Dosen ketua,@Param("dosen")Dosen dosen, @Param("dosen1") Dosen dosenNote,@Param("tahun") TahunAkademik tahunAkademik);

    List<Seminar> findByStatusAndKetuaPengujiAndTahunAkademikOrStatusAndDosenPengujiAndTahunAkademikOrStatusAndNoteDosenAndTahunAkademikOrderByStatusSemproDesc(StatusApprove statusApprove1,Dosen dosen,TahunAkademik tahunAkademik,StatusApprove statusApprove2,Dosen dosen2, TahunAkademik tahunAkademik2, StatusApprove statusApprove3,Dosen dosen3,TahunAkademik tahunAkademik3);

    List<Seminar> findByStatusAndNoteDosenAndTahunAkademikOrderByStatusSemproDesc(StatusApprove statusApprove,Dosen dosen,TahunAkademik tahunAkademik);

    @Query(value = "select round(sum(ka+ua+pa),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalA(Seminar seminar);

    @Query(value = "select round(sum(kb+ub+pb),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalB(Seminar seminar);

    @Query(value = "select round(sum(kc+uc+pc),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalC(Seminar seminar);

    @Query(value = "select round(sum(kd+ud+pd),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalD(Seminar seminar);

    @Query(value = "select round(sum(ke+ue+pe),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalE(Seminar seminar);

    @Query(value = "select round(sum(kf+uf+pf),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalF(Seminar seminar);

    @Query(value = "select round(sum(nilai_a+nilai_b+nilai_c+nilai_d+nilai_e+nilai_f),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalAkhirSkripsi(Seminar seminar);

    @Query(value = "select round(sum(nilai_a+nilai_b+nilai_c+nilai_d+nilai_e+nilai_f),2) from seminar where id = ?1", nativeQuery = true)
    BigDecimal totalAkhirSkb(Seminar seminar);

    List<Seminar> findByNoteMahasiswaAndStatusSemproNotInAndStatus(Mahasiswa mahasiswa,List<StatusApprove> statusSempro,StatusApprove statusApprove);

    @Query(value = "select * from seminar where id = ?1 and (ka = ?2 or kb = ?2 or kc = ?2 or kd = ?2 or ke = ?2 or kf = ?2 or ua = ?2 or ub = ?2 or uc = ?2 or ud = ?2 or ue = ?2 or uf = ?2 or pa = ?2 or pb = ?2 or pc = ?2 or pd = ?2 or pe = ?2 or pf = ?2)", nativeQuery = true)
    Object validasiSemproSKripsi(Seminar seminar, BigDecimal nilai);

    @Query(value = "select * from seminar where id = ?1 and (ka = ?2 or kb = ?2 or kc = ?2 or kd = ?2 or ke = ?2 or ua = ?2 or ub = ?2 or uc = ?2 or ud = ?2 or ue = ?2 or pa = ?2 or pb = ?2 or pc = ?2 or pd = ?2 or pe = ?2)", nativeQuery = true)
    Object validasiSemproStudy(Seminar seminar, BigDecimal nilai);

    @Query(value = "select s.* from seminar as s inner join note as n on s.id_note = n.id where n.id_mahasiswa = ?1 and s.status not in('HAPUS')",nativeQuery = true)
    List<Object> cekSeminar(Mahasiswa mahasiswa);

    Seminar findByStatusAndPublishAndNilaiGreaterThanAndNoteMahasiswa(StatusApprove approve,String publish,BigDecimal nilai,Mahasiswa mahasiswa);

    @Query(value = "select m.nim,m.nama,s.tanggal_ujian as tanggalSempro,coalesce (kk.nama_karyawan,'-') as ketuaSempro,coalesce (pk.nama_karyawan,'-') as pembimbingSempro ,coalesce (dk.nama_karyawan,'-') as pengujiSempro,coalesce (s.jam_mulai,'-') as jamMulai,coalesce (s.nilai,'0') as Nilai,\n" +
            "coalesce(ss.sidangs,'Belum Daftar') as statusSidang,ss.jam_mulai as mulaiSidang,ss.jam_selesai as selesaiSidang,ss.ketua as ketuaSidang,ss.pembimbing as pembimbingSidang,ss.penguji as pengujiSidang,coalesce(ss.nilai,0) as nilaiSidang, ss.tanggalSidang\n" +
            "from seminar as s inner join note as n on s.id_note = n.id inner join mahasiswa as m on n.id_mahasiswa = m.id \n" +
            "left join dosen as ket on s.ketua_penguji = ket.id left join karyawan as kk on ket.id_karyawan =kk.id \n" +
            "left join dosen as pem on n.id_dosen1 = pem.id left join karyawan as pk on pem.id_karyawan =pk.id\n" +
            "left join dosen as peng on s.dosen_penguji = peng.id left join karyawan as dk on peng.id_karyawan = dk.id\n" +
            " left join \n" +
            " (select sid.id_seminar,sid.id as sidangs, sid.nilai as nilai,kk.nama_karyawan as ketua,pk.nama_karyawan as pembimbing ,dk.nama_karyawan as penguji,sid.jam_mulai,sid.jam_selesai,sid.tanggal_ujian as tanggalSidang\n" +
            " from sidang as sid inner join seminar as se on sid.id_seminar = se.id\n" +
            " left join dosen as ket on sid.ketua_penguji = ket.id left join karyawan as kk on ket.id_karyawan =kk.id \n" +
            "left join dosen as pem on sid.pembimbing = pem.id left join karyawan as pk on pem.id_karyawan =pk.id\n" +
            "left join dosen as peng on sid.dosen_penguji = peng.id left join karyawan as dk on peng.id_karyawan = dk.id\n" +
            " where sid.akademik in ('APPROVED', 'WAITING') and sid.status_sidang in ('APPROVED', 'WAITING')) ss on ss.id_seminar = s.id\n" +
            " where s.status in ('APPROVED', 'WAITING') and m.id_prodi = ?1 and m.angkatan = ?2", nativeQuery = true)
    List<RekapTugasAkhir> rekapTugasAkhir(Prodi prodi, String angkatan);

}
