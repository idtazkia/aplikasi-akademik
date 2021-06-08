package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.payment.*;
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

    Page<Tagihan> findByStatusNotInAndMahasiswaAndTahunAkademik(List<StatusRecord> asList, Mahasiswa mhs, TahunAkademik tahunAkademik, Pageable page);
    
    Page<Tagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaOrderByNilaiJenisTagihan(StatusRecord statusRecord, Mahasiswa mhs, Pageable page);

    Tagihan findByNomor(String nomor);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan\n" +
            "where id_tahun_akademik=?1 and id_mahasiswa=?2 and status='AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanPerTahunAkademikDanMahasiswa(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) from tagihan \n" +
            "where id_mahasiswa = ?1 and status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanPerMahasiswa(String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan as a join tahun_akademik as b on a.id_tahun_akademik=b.id where b.id=?1 and a.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalTagihan(TahunAkademik tahunAkademik);

    @Query(value = "select aa.id_mahasiswa,id_tahun_akademik,nama_tahun_akademik as namaTahun,tagihan,0 as potongan,coalesce(dibayar,0)as dibayar,0 as penarikan,tagihan-coalesce(dibayar,0)as sisa from\n" +
            "(select a.id_tahun_akademik,b.nama_tahun_akademik,id_mahasiswa,sum(nilai_tagihan)as tagihan \n" +
            "from tagihan as a inner join tahun_akademik as b\n" +
            "on a.id_tahun_akademik=b.id where id_tahun_akademik = ?1 \n" +
            "and id_mahasiswa=?2 and a.status='AKTIF')aa left join\n" +
            "(select b.id_mahasiswa,sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "where b.id_tahun_akademik = ?1 and b.id_mahasiswa=?2 and b.status='AKTIF')bb on aa.id_mahasiswa=bb.id_mahasiswa", nativeQuery = true)
    List<SisaTagihanDto> sisaTagihanQuery(String idTahunAkademik, String idMahasiswa);


    @Query(value = "select aaa.*,coalesce(bbb.dibayar,0)as dibayar,nilai_tagihan-coalesce(dibayar,0) as sisa from \n" +
            "(select a.id,b.nama as namaTagihan,c.nama_tahun_akademik as namaTahun, a.status_tagihan as status,a.nilai_tagihan from tagihan as a \n" +
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



    @Query(value = "select c.id, c.nama_prodi as prodi, sum(coalesce(a.nilai_tagihan,0)) as tagihan, sum(coalesce(d.amount,0)) as dibayar, \n" +
            "coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(d.amount,0))) as sisa, \n" +
            "substr(coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(d.amount,0))) * 100/sum(coalesce(a.nilai_tagihan,0)), 1,4) as percentage from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id \n" +
            "inner join prodi as c on b.id_prodi=c.id inner join tahun_akademik as e on a.id_tahun_akademik=e.id left join pembayaran as d on d.id_tagihan=a.id \n" +
            "where a.status='AKTIF' and e.id=?1 group by c.id order by prodi asc", nativeQuery = true)
    List<DaftarTagihanPerProdiDto> listTagihanPerProdi(TahunAkademik tahunAkademik);

    @Query(value = "select c.nama_prodi as prodi, sum(coalesce(d.amount,0)) as pemasukan from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' and a.id_tahun_akademik=?3 and d.waktu_bayar between ?1 and ?2 \n" +
            "group by prodi order by prodi asc", nativeQuery = true)
    List<Object[]> listTagihanPerProdiAndDate(String tanggal1, String tanggal2, TahunAkademik tahunAkademik);

    @Query(value = "select distinct b.angkatan as angkatan, sum(coalesce(a.nilai_tagihan,0)) as tagihan, sum(coalesce(c.amount,0)) as \n" +
            "dibayar, coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(c.amount,0))) as sisa, \n" +
            "substr(coalesce(sum(coalesce(a.nilai_tagihan,0))-sum(coalesce(c.amount,0))) * 100/sum(coalesce(a.nilai_tagihan,0)), 1,4) as percentage \n" +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join tahun_akademik as d on a.id_tahun_akademik=d.id \n" +
            "left join pembayaran as c on a.id=c.id_tagihan where a.status='AKTIF' and d.id=?1 group by angkatan \n" +
            "order by angkatan asc", nativeQuery = true)
    List<DaftarTagihanPerAngkatanDto> listTagihanPerAngkatan(TahunAkademik tahunAkademik);

    @Query(value = "select distinct b.angkatan as angkatan, sum(coalesce(d.amount,0)) as pemasukan from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' and a.id_tahun_akademik=?3 and d.waktu_bayar between ?1 and ?2 \n" +
            "group by angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listTagihanPerAngkatanDate(String tanggal3, String tanggal4, TahunAkademik tahunAkademik);

    @Query(value = "select a.id as id, a.angkatan, a.nim as nim, a.nama, sum(coalesce(b.nilai_tagihan,0)) as tagihan, \n" +
            "sum(coalesce(c.amount,0)) as dibayar, sum(coalesce(b.nilai_tagihan,0))-sum(coalesce(c.amount,0)) as sisa \n" +
            "from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa left join pembayaran as c \n" +
            "on b.id=c.id_tagihan where a.id_prodi=?1 and b.status='AKTIF' and b.id_tahun_akademik=?2 group by id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByProdi(String idProdi, String idTahunAkademik);

    @Query(value = "select a.id as id, c.nama_prodi as prodi, a.nim as nim, a.nama as nama, sum(coalesce(b.nilai_tagihan,0)) as tagihan, \n" +
            "sum(coalesce(d.amount,0)) as dibayar, sum(coalesce(b.nilai_tagihan,0))-sum(coalesce(d.amount,0)) as sisa \n" +
            "from mahasiswa as a inner join tagihan as b on \n" +
            "a.id=b.id_mahasiswa inner join prodi as c on \n" +
            "a.id_prodi=c.id left join pembayaran as d on b.id=d.id_tagihan where \n" +
            "b.status='AKTIF' and a.angkatan=?1 and b.id_tahun_akademik=?2 group by id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByAngkatan(String angkatan, String idTahunAkademik);

    @Query(value = "select 'LANCAR' as keterangan, count(id_mahasiswa)as jumlah, coalesce(selisih, 100) from\n" +
            " (select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih = 0\n" +
            " union\n" +
            "select 'PERHATIAN KHUSUS' as keterangan, count(id_mahasiswa)as jumlah, coalesce(selisih, 100) from\n" +
            " (select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3)\n" +
            " union\n" +
            "select 'KURANG LANCAR' as keterangan, count(id_mahasiswa)as jumlah,coalesce(selisih, 100) from\n" +
            " (select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (4,5)\n" +
            " union\n" +
            "select 'DIRAGUKAN' as keterangan, count(id_mahasiswa)as jumlah, coalesce(selisih, 100) from\n" +
            " (select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)\n" +
            " union\n" +
            "select 'MACET' as keterangan, count(id_mahasiswa)as jumlah, coalesce(selisih, 100) from\n" +
            " (select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih > 6", nativeQuery = true)
    List<Object[]> listPiutang(String idTahunAkademik);

    @Query(value = "select nim, nama, a.* from\n" +
            "(select a.id_mahasiswa, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar,\n" +
            " TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, tagihan, coalesce(pembayaran,0) as pembayaran from\n" +
            " (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan where id_tahun_akademik = ?1 \n" +
            " and status = 'AKTIF' group by id_mahasiswa) a\n" +
            " left join\n" +
            " (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a \n" +
            " inner join tagihan as b on a.id_tagihan = b.id \n" +
            " where b.id_tahun_akademik = ?1 and a.status = 'AKTIF' and b.status = 'AKTIF'\n" +
            " group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)a\n" +
            " inner join mahasiswa as b on a.id_mahasiswa = b.id \n" +
            " where selisih = ?2", nativeQuery = true)
    List<Object[]> detailPiutang(String idTahunAkademik, String selisih);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, StatusRecord statusRecord);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatusAndLunas(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, StatusRecord statusRecord, boolean lunas);

    Tagihan findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord statusRecord, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, boolean lunas);

    List<Tagihan> findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihanOrderByMahasiswaNama(Prodi prodi, Program program, String angkatan, TahunAkademik tahunAkademik, LocalDate tanggal, StatusTagihan status);

    List<Tagihan> findByStatusNotInAndLunasAndMahasiswaAndTahunAkademik(List<StatusRecord> statusRecord, boolean lunas, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    Tagihan findByMahasiswaAndNilaiJenisTagihanAndStatusTagihanOrStatusTagihanAndStatus(Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, StatusTagihan st1, StatusTagihan st2, StatusRecord statusRecord);

}
