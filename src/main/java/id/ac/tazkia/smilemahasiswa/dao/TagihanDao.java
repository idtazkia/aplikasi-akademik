package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.payment.BiayaMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarBiayaDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.NilaiCicilanDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.SisaTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TagihanDao extends PagingAndSortingRepository<Tagihan, String> {

    Page<Tagihan> findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(List<StatusRecord> asList, String search, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaAndTahunAkademik(StatusRecord statusRecord, Mahasiswa mhs, TahunAkademik tahunAkademik, Pageable page);
    
    Page<Tagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaOrderByNilaiJenisTagihan(StatusRecord statusRecord, Mahasiswa mhs, Pageable page);

    Tagihan findByNomor(String nomor);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan\n" +
            "where id_tahun_akademik=?1 and id_mahasiswa=?2 and status='AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanPerTahunAkademikDanMahasiswa(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) from tagihan \n" +
            "where id_mahasiswa = ?1 and status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanPerMahasiswa(String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan where status='AKTIF'", nativeQuery = true)
    BigDecimal totalTagihan();

    @Query(value = "select aa.id_mahasiswa,id_tahun_akademik,nama_tahun_akademik as namaTahun,tagihan,0 as potongan,coalesce(dibayar,0)as dibayar,0 as penarikan,tagihan-coalesce(dibayar,0)as sisa from\n" +
            "(select a.id_tahun_akademik,b.nama_tahun_akademik,id_mahasiswa,sum(nilai_tagihan)as tagihan \n" +
            "from tagihan as a inner join tahun_akademik as b\n" +
            "on a.id_tahun_akademik=b.id where id_tahun_akademik = ?1 \n" +
            "and id_mahasiswa=?2 and a.status='AKTIF')aa left join\n" +
            "(select b.id_mahasiswa,sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "where b.id_tahun_akademik = ?1 and b.id_mahasiswa=?2 and b.status='AKTIF')bb on aa.id_mahasiswa=bb.id_mahasiswa", nativeQuery = true)
    List<SisaTagihanDto> sisaTagihanQuery(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select a.id, a.id_mahasiswa, a.id_tahun_akademik, d.nama as namaTagihan, a.nilai_tagihan as tagihan, \n" +
            "coalesce(b.amount,0) as dibayar, a.lunas as lunas from tagihan as a inner join nilai_jenis_tagihan as c on \n" +
            "a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id left join pembayaran as \n" +
            "b on a.id=b.id_tagihan where a.id_tahun_akademik=?1 and a.id_mahasiswa=?2 and a.status!='HAPUS'", nativeQuery = true)
    List<DaftarBiayaDto> daftarBiaya(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select aaa.*,coalesce(bbb.dibayar,0)as dibayar,nilai_tagihan-coalesce(dibayar,0) as sisa from \n" +
            "(select a.id,b.nama as namaTagihan,c.nama_tahun_akademik as namaTahun,a.nilai_tagihan from tagihan as a \n" +
            "inner join nilai_jenis_tagihan as g on a.id_nilai_jenis_tagihan = g.id \n" +
            "inner join jenis_tagihan as b on g.id_jenis_tagihan=b.id \n" +
            "inner join tahun_akademik as c on a.id_tahun_akademik=c.id where a.id_mahasiswa=?1 and a.status!='HAPUS' \n" +
            "order by b.nama)aaa \n" +
            "left join \n" +
            "(select sum(amount)as dibayar,aa.id_tagihan from pembayaran as aa inner join tagihan as bb on \n" +
            "aa.id_tagihan = bb.id where bb.id_mahasiswa=?1 and bb.status!='HAPUS' and aa.status='AKTIf' \n" +
            "group by id_tagihan)bbb \n" +
            "on aaa.id=bbb.id_tagihan" , nativeQuery = true)
    List<BiayaMahasiswaDto> biayaMahasiswa(String idMahasiswa);

    @Query(value = "select a.id as idTagihan, b.id as idCicilan, a.nilai_tagihan as nilai, round(a.nilai_tagihan / b.banyak_cicilan, 0) \n" +
            "as nilaiCicilan, a.tanggal_jatuh_tempo as jatuhTempo from tagihan as a inner join request_cicilan as b \n" +
            "on a.id=b.id_tagihan where a.id=?1 and a.status='AKTIF' and b.status='AKTIF'", nativeQuery = true)
    NilaiCicilanDto pembagianNilaiCicilan(String idTagihan);

    @Query(value = "select c.nama_prodi as prodi, sum(coalesce(a.nilai_tagihan,0)) as tagihan, sum(coalesce(d.amount,0)) as \n" +
            "dibayar, coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(d.amount,0))) as sisa from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' group by prodi order by prodi asc", nativeQuery = true)
    List<Object[]> listTagihanPerProdi();

    @Query(value = "select c.nama_prodi as prodi, sum(coalesce(d.amount,0)) as pemasukan from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' and d.waktu_bayar between ?1 and ?2 \n" +
            "group by prodi order by prodi asc", nativeQuery = true)
    List<Object[]> listTagihanPerProdiAndDate(String tanggal1, String tanggal2);

    @Query(value = "select distinct b.angkatan as angkatan, sum(coalesce(a.nilai_tagihan,0)) as tagihan, sum(coalesce(c.amount,0)) as \n" +
            "dibayar, coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(c.amount,0))) as sisa from tagihan as a inner join \n" +
            "mahasiswa as b on a.id_mahasiswa=b.id left join pembayaran as c on a.id=c.id_tagihan \n" +
            "where a.status='AKTIF' group by angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listTagihanPerAngkatan();

    @Query(value = "select distinct b.angkatan as angkatan, sum(coalesce(d.amount,0)) as pemasukan from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' and d.waktu_bayar between ?1 and ?2 \n" +
            "group by angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listTagihanPerAngkatanDate(String tanggal3, String tanggal4);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndLunasAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, boolean lunas, StatusRecord statusRecord);

    Tagihan findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihan(StatusRecord statusRecord, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan);

    List<Tagihan> findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihan(Prodi prodi, Program program, String angkatan, TahunAkademik tahunAkademik, LocalDate tanggal, StatusTagihan status);

    Tagihan findByMahasiswaAndNilaiJenisTagihanAndStatusTagihanOrStatusTagihanAndStatus(Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, StatusTagihan st1, StatusTagihan st2, StatusRecord statusRecord);

}
