package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.ujian.ListTahfidz;
import id.ac.tazkia.smilemahasiswa.dto.ujian.Tahfidz;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface UjianTahfidzKompreDao extends PagingAndSortingRepository<UjianTahfidzKompre, String> {
    List<UjianTahfidzKompre> findByStatusAndUrutanOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan);

    @Query(value = "SELECT * FROM ujian_tahfidz_kompre where id_mahasiswa = ?1 and jenis_ujian = ?2 order by tanggal_pengajuan desc limit 1", nativeQuery = true)
    UjianTahfidzKompre getUjianTerakhir(Mahasiswa mahasiswa,String jenis);

    UjianTahfidzKompre findByMahasiswaAndJenisUjianAndStatusLulus(Mahasiswa mhs, String jenis, StatusRecord status);

    @Query(value = "SELECT * FROM ujian_tahfidz_kompre WHERE id_mahasiswa = ?1 AND jenis_ujian = ?2 AND status_lulus = 'TIDAK_LULUS' LIMIT 1", nativeQuery = true)
    UjianTahfidzKompre findAnyTidakLulusByMahasiswaAndJenisUjian(Mahasiswa mahasiswa, String jenis);

    List<UjianTahfidzKompre> findByMahasiswaAndJenisUjian(Mahasiswa mahasiswa,String  ujian);

    List<UjianTahfidzKompre> findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord statusRecord, String statusAdmin);
    List<UjianTahfidzKompre> findByStatusAndUrutanAndDosenKpsOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan, Dosen dosen);

    //admin
    Page<UjianTahfidzKompre> findByStatusAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, String search,String seacrch, Pageable pageable);

    Page<UjianTahfidzKompre> findByStatusAndStatusApprovalAdminNotInAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,List<String> approve,Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, String search ,Pageable pageable);

    Page<UjianTahfidzKompre> findByStatusAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,String jenisUjian,TahunAkademik tahunAkademik, String search, String seacrch, Pageable pageable);

    //Kps
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Integer urutan, Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Integer urutan, Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, String search,String seacrch, Pageable pageable);

    Page<UjianTahfidzKompre> findByStatusAndUrutanAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan, String jenisUjian,TahunAkademik tahunAkademik, String search, String seacrch, Pageable pageable);


    List<UjianTahfidzKompre> findByStatusAndMahasiswaAndJenisUjianOrderByTanggalPengajuanDescStatusApprovalAdminAsc(StatusRecord statusRecord, Mahasiswa mahasiswa, String jenisUjian);
    List<UjianTahfidzKompre> findByStatusAndMahasiswaAndJenisUjianOrderByTanggalPengajuanAsc(StatusRecord statusRecord, Mahasiswa mahasiswa, String jenisUjian);

    @Query(value = "SELECT count(status_approval_admin) FROM ujian_tahfidz_kompre where id_mahasiswa = ?1 and jenis_ujian = ?2 and status_approval_admin = 'REJECTED'", nativeQuery = true)
    Integer jumlahRejected(Mahasiswa mahasiswa, String jenisUjian);

    @Query(value = "select count(a.grade) from krs_detail as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "inner join jadwal as c on a.id_jadwal = c.id\n" +
            "where a.id_mahasiswa = ?1 and a.status = 'AKTIF' and a.grade = 'D'", nativeQuery = true)
    Integer jumlahNilaiD(Mahasiswa mahasiswa);

    @Query(value = "select coalesce(max(ujian_ke), 1 ) as ujian from ujian_tahfidz_kompre where id_mahasiswa = ?1 and jenis_ujian = ?2", nativeQuery = true)
    Integer jumlahUjian(Mahasiswa mahasiswa,String jenis);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND a.finalisasi ='FINAL' AND a.id_mahasiswa IS NOT NULL AND id_mahasiswa='2010101003' and a.grade not in ('E')", nativeQuery = true)
    BigDecimal ipkCheck(Mahasiswa mahasiswa);

    @Query(value = "select u.id as id, m.nim as nim,m.nama as nama ,u.jenis_ujian as ujian,u.tanggal_pengajuan as tanggal,u.status_approval_admin as status,u.status_lulus as statusUjian from ujian_tahfidz_kompre as u\n" +
            " inner join mahasiswa as m on u.id_mahasiswa = m.id where m.id = ?1 and u.jenis_ujian = ?2 and u.status = 'AKTIF' order by u.tanggal_pengajuan asc", nativeQuery = true)
    List<Tahfidz> getListMahasiswaTahfidz(Mahasiswa mahasiswa, String jenis);

    @Query(value = "select u.id as id, m.nim as nim ,m.nama as nama,u.jenis_ujian as ujian,u.ujian_ke as ujianKe,u.tanggal_pengajuan as tanggal,u.status_approval_admin as status, u.file_surat as surat,u.status_lulus as statusUjian from ujian_tahfidz_kompre as u\n" +
            " inner join mahasiswa as m on u.id_mahasiswa = m.id where m.status = 'AKTIF' and u.id_tahun_akademik = ?1 and u.jenis_ujian\n" +
            "  = ?2 and u.id_prodi = ?3 and u.status_approval_admin not in ('REJECTED') order by u.tanggal_pengajuan desc", nativeQuery = true)
    List<ListTahfidz> getListTahfidzAdmin(TahunAkademik tahunAkademik, String ujian, Prodi prodi);

    @Query(value = "select u.id as id, m.nim as nim ,m.nama as nama,u.jenis_ujian as ujian,u.ujian_ke as ujianKe,u.tanggal_pengajuan as tanggal,u.status_approval_admin as status, u.file_surat as surat,u.status_lulus as statusUjian from ujian_tahfidz_kompre as u\n" +
            " inner join mahasiswa as m on u.id_mahasiswa = m.id where m.status = 'AKTIF' and u.id_tahun_akademik = ?1 and u.jenis_ujian\n" +
            "  = ?2 and u.id_prodi = ?3 and u.status_approval_admin not in ('REJECTED') order by u.tanggal_pengajuan desc LIMIT ?4 OFFSET ?5 ", nativeQuery = true)
    List<ListTahfidz> getListTahfidzAdminLimit(TahunAkademik tahunAkademik, String ujian, Prodi prodi, Integer limit, Integer offset);

    @Query(value = "select u.id as id, m.nim as nim ,m.nama as nama,u.jenis_ujian as ujian,u.ujian_ke as ujianKe,u.tanggal_pengajuan as tanggal,u.status_approval_admin as status, u.file_surat as surat,u.status_lulus as statusUjian from ujian_tahfidz_kompre as u\n" +
            " inner join mahasiswa as m on u.id_mahasiswa = m.id where m.status = 'AKTIF' and u.id_tahun_akademik = ?1 and u.jenis_ujian\n" +
            "  = ?2 and u.id_prodi = ?3 and u.status_approval_admin not in ('REJECTED') and (m.nim like %?4% or m.nama like %?4%) order by u.tanggal_pengajuan desc ", nativeQuery = true)
    List<ListTahfidz> getListTahfidzAdminSearch(TahunAkademik tahunAkademik, String ujian, Prodi prodi,String search);

    @Query(value = "select u.id as id, m.nim as nim ,m.nama as nama,u.jenis_ujian as ujian,u.ujian_ke as ujianKe,u.tanggal_pengajuan as tanggal,u.status_approval_admin as status, u.file_surat as surat,u.status_lulus as statusUjian from ujian_tahfidz_kompre as u\n" +
            " inner join mahasiswa as m on u.id_mahasiswa = m.id where m.status = 'AKTIF' and u.id_tahun_akademik = ?1 and u.jenis_ujian\n" +
            "  = ?2 and u.id_prodi = ?3 and u.status_approval_admin not in ('REJECTED') and (m.nim like %?4% or m.nama like %?4%) order by u.tanggal_pengajuan desc LIMIT ?5 OFFSET ?6 ", nativeQuery = true)
    List<ListTahfidz> getListTahfidzAdminSearchLimit(TahunAkademik tahunAkademik, String ujian, Prodi prodi,String search, Integer limit, Integer offset);

    @Query(value = "SELECT * FROM ujian_tahfidz_kompre where id_mahasiswa = ?1 and jenis_ujian = ?2 and status = 'AKTIF' order by tanggal_pengajuan desc limit 1", nativeQuery = true)
    UjianTahfidzKompre checkDataTerakhir(Mahasiswa mahasiswa, String jenis);


}
