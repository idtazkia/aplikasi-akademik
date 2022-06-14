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
            "where id_tahun_akademik=?1 and id_mahasiswa=?2 and status!='HAPUS'", nativeQuery = true)
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

    @Query(value = "select a.angkatan as angkatan, coalesce(tagihan,0) as tagihan,  coalesce(dibayar,0) as dibayar, coalesce(tagihan-dibayar,0) as sisa, substr((coalesce(tagihan,0)-coalesce(dibayar,0)) * 100/coalesce(tagihan,0), 1,4) as percentage  from\n" +
            "(select a.angkatan as angkatan, sum(coalesce(a.tagihan,0)) as tagihan from\n" +
            "(select a.id,b.angkatan as angkatan, coalesce(a.nilai_tagihan,0) as tagihan\n" +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as u on b.id_prodi = u.id inner join tahun_akademik as d on a.id_tahun_akademik=d.id where a.status='AKTIF' and d.id=?1)as a group by angkatan) a\n" +
            "left join \n" +
            "(select angkatan, sum(a.amount)as dibayar from pembayaran as a inner join tagihan as b on b.id=a.id_tagihan inner join mahasiswa as c on b.id_mahasiswa = c.id where a.status = 'AKTIF' and b.status='AKTIF' and c.status = 'AKTIF' and b.id_tahun_akademik=?1  group by c.angkatan) as b on a.angkatan = b.angkatan\n" +
            "order by angkatan asc", nativeQuery = true)
    List<DaftarTagihanPerAngkatanDto> listTagihanPerAngkatan(TahunAkademik tahunAkademik);

    @Query(value = "select distinct b.angkatan as angkatan, sum(coalesce(d.amount,0)) as pemasukan from tagihan as a \n" +
            "inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id left join pembayaran \n" +
            "as d on d.id_tagihan=a.id where a.status='AKTIF' and a.id_tahun_akademik=?3 and d.waktu_bayar between ?1 and ?2 \n" +
            "group by angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listTagihanPerAngkatanDate(String tanggal3, String tanggal4, TahunAkademik tahunAkademik);

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') from (select a.id,a.id_tagihan,angkatan,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status from (select a.id as id, b.id as id_tagihan, a.angkatan as angkatan, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa where b.status='AKTIF' and a.id_prodi=?1 and b.id_tahun_akademik=?2)a left join (select id_tagihan,sum(amount)as amount from pembayaran group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join mahasiswa_beasiswa as b on a.id=b.id_mahasiswa left join beasiswa as c on b.id_beasiswa=c.id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByProdi(String idProdi, String idTahunAkademik);

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') from (select a.id,a.id_tagihan,a.prodi,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status from (select a.id as id, b.id as id_tagihan, c.nama_prodi as prodi, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join prodi as c on a.id_prodi=c.id  where b.status='AKTIF' and a.angkatan=?1 and b.id_tahun_akademik=?2)a left join (select id_tagihan,sum(amount)as amount from pembayaran group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join mahasiswa_beasiswa as b on a.id = b.id_mahasiswa left join beasiswa as c on b.id_beasiswa = c.id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByAngkatan(String angkatan, String idTahunAkademik);

    @Query(value = "select 'LANCAR' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Lunas dan tepat waktu' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih <= 1\n" +
            "union\n" +
            "select 'KURANG LANCAR' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Lunas dan tidak tepat waktu 2 - 5 bulan' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)\n" +
            "union\n" +
            "select 'PERHATIAN KHUSUS' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Belum Lunas melebihi 2 bulan sampai 5 bulan' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)\n" +
            "union\n" +
            "select 'HAMPIR DIRAGUKAN' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Lunas di bulan ke 6' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)\n" +
            "union\n" +
            "select 'DIRAGUKAN' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Belum Lunas sampai bulan ke 6' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)\n" +
            "union\n" +
            "select 'SANGAT DIRAGUKAN' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Belum Lunas lebih dari 6 bulan' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa where selisih > 6\n" +
            "union\n" +
            "select 'TERSENDAT' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Akumulasi Tagihan dan Sudah lunas' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa \n" +
            "union\n" +
            "select 'MACET' as status, count(id_mahasiswa)as jumlah, coalesce(selisih, 100),'Akumulasi Tagihan dan Belum lunas' as keterangan from (select a.id_mahasiswa, tanggal_pembuatan,\n" +
            "date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,\n" +
            "TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, \n" +
            "pembayaran from (select id_mahasiswa, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join \n" +
            "(select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join tahun_akademik as c on b.id_tahun_akademik = c.id\n" +
            "where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b \n" +
            "on a.id_mahasiswa = b.id_mahasiswa)aa", nativeQuery = true)
    List<Object[]> listPiutang(String kodeTahunAkademik);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, a.nim, a.nama, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, c.nim, c.nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih <= 1", nativeQuery = true)
    List<Object[]> detailLancar(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, c.nim, c.nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)", nativeQuery = true)
    List<Object[]> detailKurangLancar(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)", nativeQuery = true)
    List<Object[]> detailPerhatianKhusus(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)", nativeQuery = true)
    List<Object[]> detailHampirDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)", nativeQuery = true)
    List<Object[]> detailDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih > 6", nativeQuery = true)
    List<Object[]> detailSangatDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa", nativeQuery = true)
    List<Object[]> detailTersendat(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0) from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= '20211' and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa", nativeQuery = true)
    List<Object[]> detailMacet(String kodeTahunAkademik, String status);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join " +
            "jenis_tagihan as d on c.id_jenis_tagihan=d.id where a.status='AKTIF' and d.kode='23' and a.lunas = true and a.id_mahasiswa=?1 \n" +
            "and a.id_tahun_akademik=?2 order by a.tanggal_pembuatan limit 1", nativeQuery = true)
    Tagihan tagihanSp(String idMahasiswa, String idTahun);

    @Query(value = "select e.nim, e.nama, e.email_pribadi as emailPribadi, e.email_tazkia as emailTazkia from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik=b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id inner join mahasiswa as e on a.id_mahasiswa=e.id inner join s_user as f on e.id_user=f.id inner join prodi as g on e.id_prodi=g.id where akumulasi_pembayaran = 0 and status_tagihan not in('LUNAS','DITANGGUHKAN') and a.status='AKTIF' and b.id=?1 and lunas is false and d.kode in('14','22', '40', '44') order by nim", nativeQuery = true)
    List<DisableMahasiswaDto> disableMahasiswa(TahunAkademik tahunAkademik);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatusAndLunas(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, StatusRecord statusRecord, boolean lunas);

    Tagihan findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord statusRecord, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, boolean lunas);

    List<Tagihan> findByNilaiJenisTagihanProdiAndNilaiJenisTagihanProgramAndNilaiJenisTagihanAngkatanAndTahunAkademikAndTanggalPembuatanAndStatusTagihanOrderByMahasiswaNama(Prodi prodi, Program program, String angkatan, TahunAkademik tahunAkademik, LocalDate tanggal, StatusTagihan status);

    List<Tagihan> findByStatusNotInAndLunasAndMahasiswaAndTahunAkademik(List<StatusRecord> statusRecord, boolean lunas, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<Tagihan> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    List<Tagihan> findByStatusAndStatusTagihanNotInAndMahasiswaAndLunas(StatusRecord statusRecord, List<StatusTagihan> status, Mahasiswa mahasiswa, boolean lunas);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, TahunAkademik tahunAkademik, boolean lunas, StatusRecord statusRecord);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanKodeInAndTahunAkademikAndStatus(Mahasiswa mahasiswa, List<String> kode, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa = b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id where a.id_tahun_akademik=?1 and b.id_prodi=?2 and b.id_program=?3 and b.angkatan=?4 and a.status='AKTIF' and status_tagihan in('AKTIF', 'DITANGGUHKAN') and akumulasi_pembayaran='0' and d.kode not in('02oke','03','50') and nomor is not null order by nim", nativeQuery = true)
    List<Tagihan> generatePotongan(TahunAkademik tahunAkademik, String idProid, String idProgram, String angkatan);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id where a.status_tagihan='AKTIF' and lunas='0' and b.nim=?1 and status = 'AKTIF'", nativeQuery = true)
    List<Tagihan> cekTagihanLunas(String nim);

}
