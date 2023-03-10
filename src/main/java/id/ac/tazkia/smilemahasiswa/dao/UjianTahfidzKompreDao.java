package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface UjianTahfidzKompreDao extends PagingAndSortingRepository<UjianTahfidzKompre, String> {
    List<UjianTahfidzKompre> findByStatusAndUrutanOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan);

    List<UjianTahfidzKompre> findByStatusAndStatusApprovalAdminOrderByTanggalPengajuanDesc(StatusRecord statusRecord, String statusAdmin);
    List<UjianTahfidzKompre> findByStatusAndUrutanAndDosenKpsOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan, Dosen dosen);

    //admin
    Page<UjianTahfidzKompre> findByStatusAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, String search,String seacrch, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,String jenisUjian,TahunAkademik tahunAkademik, String search, String seacrch, Pageable pageable);

    //Kps
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Integer urutan, Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndProdiAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord,Integer urutan, Prodi prodi ,String jenisUjian,TahunAkademik tahunAkademik, String search,String seacrch, Pageable pageable);

    Page<UjianTahfidzKompre> findByStatusAndUrutanAndJenisUjianAndTahunAkademikOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan,String jenisUjian,TahunAkademik tahunAkademik, Pageable pageable);
    Page<UjianTahfidzKompre> findByStatusAndUrutanAndJenisUjianAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimOrderByTanggalPengajuanDesc(StatusRecord statusRecord, Integer urutan, String jenisUjian,TahunAkademik tahunAkademik, String search, String seacrch, Pageable pageable);


    List<UjianTahfidzKompre> findByStatusAndMahasiswaAndJenisUjian(StatusRecord statusRecord, Mahasiswa mahasiswa, String jenisUjian);

    @Query(value = "SELECT count(status_approval_admin) FROM ujian_tahfidz_kompre where id_mahasiswa = ?1 and jenis_ujian = ?2 and status_approval_admin = 'REJECTED'", nativeQuery = true)
    Integer jumlahRejected(Mahasiswa mahasiswa, String jenisUjian);

    @Query(value = "select count(a.grade) from krs_detail as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id\n" +
            "inner join jadwal as c on a.id_jadwal = c.id\n" +
            "where a.id_mahasiswa = ?1 and a.status = 'AKTIF' and a.grade = 'D'", nativeQuery = true)
    Integer jumlahNilaiD(Mahasiswa mahasiswa);

    @Query(value = "SELECT ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id WHERE a.status='AKTIF' AND a.finalisasi ='FINAL' AND a.id_mahasiswa IS NOT NULL AND id_mahasiswa='2010101003' and a.grade not in ('E')", nativeQuery = true)
    BigDecimal ipkCheck(Mahasiswa mahasiswa);

}
