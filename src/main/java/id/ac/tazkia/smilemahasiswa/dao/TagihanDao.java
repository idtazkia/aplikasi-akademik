package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.payment.*;
import id.ac.tazkia.smilemahasiswa.dto.prediksitest.RekapTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface TagihanDao extends PagingAndSortingRepository<Tagihan, String> {

    Page<Tagihan> findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(List<StatusRecord> asList, String search, Pageable page);

    Page<Tagihan> findByStatusNotInAndMahasiswaAndTahunAkademik(List<StatusRecord> asList, Mahasiswa mhs, TahunAkademik tahunAkademik, Pageable page);
    
    Page<Tagihan> findByStatusNotInAndMahasiswaNamaContainingIgnoreCaseOrStatusNotInAndMahasiswaNimContainingIgnoreCaseOrStatusNotInAndMahasiswaIdProdiNamaProdiContainingIgnoreCaseOrderByTanggalPembuatanDesc(List<StatusRecord> asList, String nama, List<StatusRecord> status, String nim, List<StatusRecord> listStatus, String prodi, Pageable page);

    Page<Tagihan> findByStatusNotInOrderByTanggalPembuatanDesc(List<StatusRecord> asList, Pageable page);

    Page<Tagihan> findByStatusNotInAndTahunAkademikAndMahasiswaNamaContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndMahasiswaNimContainingIgnoreCaseOrStatusNotInAndTahunAkademikAndMahasiswaIdProdiNamaProdiContainingIgnoreCaseOrderByTanggalPembuatanDesc(List<StatusRecord> asList, TahunAkademik tahun, String nama, List<StatusRecord> status, TahunAkademik akademik, String nim, List<StatusRecord> listStatus, TahunAkademik tahunAkademik, String prodi, Pageable page);

    Page<Tagihan> findByStatusNotInAndTahunAkademikOrderByTanggalPembuatanDesc(List<StatusRecord> asList, TahunAkademik tahunAkademik, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaOrderByNilaiJenisTagihan(StatusRecord statusRecord, Mahasiswa mhs, Pageable page);

    @Query("select t from Tagihan t where t.nilaiJenisTagihan.jenisTagihan.id = 'du2017' and t.tahunAkademik = :tahun and t.status <> 'HAPUS' and t.nilaiJenisTagihan.status = 'AKTIF'")
    Page<Tagihan> listTagihanDaftarUlang(@Param("tahun") TahunAkademik tahunAkademik, Pageable page);

    Tagihan findByNomor(String nomor);

    Tagihan findByNomorAndStatusTagihan(String nomor, StatusTagihan statusTagihan);

    List<Tagihan> findByStatusAndTahunAkademikAndZahirExport(StatusRecord status, TahunAkademik tahun, AkuntansiExport akuntansiExport);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan\n" +
            "where id_tahun_akademik=?1 and id_mahasiswa=?2 and status!='HAPUS'", nativeQuery = true)
    BigDecimal totalTagihanPerTahunAkademikDanMahasiswa(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) from tagihan where id_mahasiswa = ?1 and status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanPerMahasiswa(String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan as a join tahun_akademik as b on a.id_tahun_akademik=b.id where b.id=?1 and a.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalTagihan(TahunAkademik tahunAkademik);

    @Query(value = "select aa.id_mahasiswa,id_tahun_akademik,nama_tahun_akademik as namaTahun,tagihan,0 as potongan,coalesce(dibayar,0)as dibayar,0 as penarikan,tagihan-coalesce(dibayar,0)as sisa from\n" +
            "(select a.id_tahun_akademik,b.nama_tahun_akademik,id_mahasiswa,sum(nilai_tagihan)as tagihan \n" +
            "from tagihan as a inner join tahun_akademik as b\n" +
            "on a.id_tahun_akademik=b.id where id_tahun_akademik = ?1 \n" +
            "and id_mahasiswa=?2 and a.status in('AKTIF', 'NONAKTIF'))aa left join\n" +
            "(select b.id_mahasiswa,sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "where b.id_tahun_akademik = ?1 and b.id_mahasiswa=?2 and b.status in('AKTIF', 'NONAKTIF'))bb on aa.id_mahasiswa=bb.id_mahasiswa", nativeQuery = true)
    List<SisaTagihanDto> sisaTagihanQuery(String idTahunAkademik, String idMahasiswa);


    @Query(value = "select aaa.*,coalesce(bbb.dibayar,0)as dibayar,nilai_tagihan-coalesce(dibayar,0) as sisa from \n" +
            "(select a.id,b.nama as namaTagihan,c.nama_tahun_akademik as namaTahun, a.status_tagihan as status,a.nilai_tagihan from tagihan as a \n" +
            "inner join nilai_jenis_tagihan as g on a.id_nilai_jenis_tagihan = g.id \n" +
            "inner join jenis_tagihan as b on g.id_jenis_tagihan=b.id \n" +
            "inner join tahun_akademik as c on a.id_tahun_akademik=c.id where a.id_mahasiswa=?1 and a.status!='HAPUS' and a.status_tagihan != 'BEASISWA' \n" +
            "order by b.nama)aaa \n" +
            "left join \n" +
            "(select sum(amount)as dibayar,aa.id_tagihan from pembayaran as aa inner join tagihan as bb on \n" +
            "aa.id_tagihan = bb.id where bb.id_mahasiswa=?1 and bb.status!='HAPUS' and aa.status='AKTIf' \n" +
            "group by id_tagihan)bbb \n" +
            "on aaa.id=bbb.id_tagihan order by namaTahun desc" , nativeQuery = true)
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

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') as beasiswa from (select a.id,a.id_tagihan,angkatan,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status, a.nama_program as program from (select a.id as id, b.id as id_tagihan, a.angkatan as angkatan, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan, pr.nama_program from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join program as pr on a.id_program=pr.id where b.status='AKTIF' and a.id_prodi=?1 and b.id_tahun_akademik=?2)a left join (select id_tagihan,sum(amount)as amount from pembayaran group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join mahasiswa_beasiswa as b on a.id=b.id_mahasiswa left join beasiswa as c on b.id_beasiswa=c.id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByProdi(String idProdi, String idTahunAkademik);

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') as beasiswa from (select a.id,a.id_tagihan,a.prodi,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status, a.nama_program as program from (select a.id as id, b.id as id_tagihan, c.nama_prodi as prodi, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan, pr.nama_program from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join prodi as c on a.id_prodi=c.id inner join program as pr on a.id_program=pr.id where b.status='AKTIF' and a.angkatan=?1 and b.id_tahun_akademik=?2)a left join (select id_tagihan,sum(amount)as amount from pembayaran group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join (select * from mahasiswa_beasiswa where status='AKTIF') as b on a.id = b.id_mahasiswa left join (select * from beasiswa where status='AKTIF') as c on b.id_beasiswa = c.id order by nim", nativeQuery = true)
    List<Object[]> listTagihanPerMahasiswaByAngkatan(String angkatan, String idTahunAkademik);

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') as beasiswa from (select a.id,a.id_tagihan,a.prodi,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status from (select a.id as id, b.id as id_tagihan, c.nama_prodi as prodi, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join prodi as c on a.id_prodi=c.id  where b.status='AKTIF' and a.angkatan=?1 and a.status_aktif='AKTIF')a left join (select id_tagihan,sum(amount)as amount from pembayaran group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join (select * from mahasiswa_beasiswa where status='AKTIF') as b on a.id = b.id_mahasiswa left join (select * from beasiswa where status='AKTIF') as c on b.id_beasiswa = c.id order by nim", nativeQuery = true)
    List<Object[]> listAllTagihanPerMahasiswaByAngkatan(String angkatan);

    @Query(value = "select a.*, coalesce(c.nama_beasiswa, '-') as beasiswa from (select a.id,a.id_tagihan,a.prodi,a.nim,a.nama,sum(a.tagihan) as tagihan,coalesce(sum(b.amount),0) as dibayar, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, a.status from (select a.id as id, b.id as id_tagihan, c.nama_prodi as prodi, a.nim as nim, a.nama as nama, a.status_aktif as status,b.nilai_tagihan as tagihan from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join prodi as c on a.id_prodi=c.id  where b.status='AKTIF' and a.status_aktif='AKTIF' and a.angkatan=?1 and b.id_tahun_akademik=?2)a left join (select id_tagihan,sum(amount)as amount from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id where b.status='AKTIF' and b.id_tahun_akademik=?2 group by id_tagihan) as b on a.id_tagihan = b.id_tagihan group by a.nim) as a left join (select * from mahasiswa_beasiswa where status='AKTIF') as b on a.id = b.id_mahasiswa left join (select * from beasiswa where status='AKTIF') as c on b.id_beasiswa = c.id order by nim", nativeQuery = true)
    List<Object[]> listAllTagihanPerMahasiswaByAngkatanDanTahunAkademik(String angkatan, String tahunAkademik);

    @Query(value = "select a.id_tagihan, nama_tahun_akademik as tahunAkademik, nama, tanggal_pembuatan, coalesce(tagihan,0) as tagihan, coalesce(waktu_bayar,'-') as waktuBayar, coalesce(amount,0) as amount, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa, coalesce(tagihanAsli, 0) as tagihanAsli, sum(coalesce(a.tagihanAsli,0))-sum(coalesce(b.amount,0)) as sisaTagihanAsli from (select b.id as id_tagihan, e.nama_tahun_akademik, d.nama, b.tanggal_pembuatan, b.nilai_tagihan as tagihan, b.nilai_tagihan_asli as tagihanAsli from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id inner join tahun_akademik as e on b.id_tahun_akademik=e.id where a.nim=?1 and b.status <> 'HAPUS' order by nama_tahun_akademik)a left join (select id_tagihan, waktu_bayar, sum(amount) as amount from pembayaran group by id_tagihan) as b on a.id_tagihan=b.id_tagihan group by a.id_tagihan order by a.nama_tahun_akademik, tanggal_pembuatan", nativeQuery = true)
    List<Object[]> listDetailRekapTagihanPerMahasiswa(String nim);

    @Query(value = "select a.id_tagihan, nama_tahun_akademik as tahunAkademik, nama, tanggal_pembuatan, coalesce(tagihan,0) as tagihan, coalesce(waktu_bayar,'-') as waktuBayar, coalesce(amount,0) as amount, sum(coalesce(a.tagihan,0))-sum(coalesce(b.amount,0)) as sisa from (select b.id as id_tagihan, e.nama_tahun_akademik, d.nama, b.tanggal_pembuatan, b.nilai_tagihan as tagihan from mahasiswa as a inner join tagihan as b on a.id=b.id_mahasiswa inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id inner join tahun_akademik as e on b.id_tahun_akademik=e.id where a.nim=?1 and b.status <> 'HAPUS' and e.id=?2 order by nama_tahun_akademik)a left join (select id_tagihan, waktu_bayar, sum(amount) as amount from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id where b.id_tahun_akademik=?2 group by id_tagihan) as b on a.id_tagihan=b.id_tagihan group by a.id_tagihan order by a.nama_tahun_akademik, tanggal_pembuatan", nativeQuery = true)
    List<Object[]> listDetailRekapTagihanPerMahasiswaByTahunAkademik(String nim, String tahunAkademik);

    @Query(value = "select a.angkatan as angkatan, coalesce(tagihan,0) as tagihan,  coalesce(dibayar,0) as dibayar, coalesce(tagihan-dibayar,0) as sisa from (select a.angkatan as angkatan, sum(coalesce(a.tagihan,0)) as tagihan from (select a.id,b.angkatan as angkatan, coalesce(a.nilai_tagihan,0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as u on b.id_prodi = u.id inner join tahun_akademik as d on a.id_tahun_akademik=d.id where a.status='AKTIF' and b.status_aktif='AKTIF')as a group by angkatan) a left join (select angkatan, sum(a.amount)as dibayar from pembayaran as a inner join tagihan as b on b.id=a.id_tagihan inner join mahasiswa as c on b.id_mahasiswa = c.id where a.status = 'AKTIF' and b.status='AKTIF' and c.status = 'AKTIF' and c.status_aktif='AKTIF' group by c.angkatan) as b on a.angkatan = b.angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listAllTagihanPerAngkatan();

    @Query(value = "select a.angkatan as angkatan, coalesce(tagihan,0) as tagihan,  coalesce(dibayar,0) as dibayar, coalesce(tagihan-dibayar,0) as sisa from (select a.angkatan as angkatan, sum(coalesce(a.tagihan,0)) as tagihan from (select a.id,b.angkatan as angkatan, coalesce(a.nilai_tagihan,0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as u on b.id_prodi = u.id inner join tahun_akademik as d on a.id_tahun_akademik=d.id where a.status='AKTIF' and b.status_aktif='AKTIF' and d.id=?1)as a group by angkatan) a left join (select angkatan, sum(a.amount)as dibayar from pembayaran as a inner join tagihan as b on b.id=a.id_tagihan inner join mahasiswa as c on b.id_mahasiswa = c.id where a.status = 'AKTIF' and b.status='AKTIF' and c.status = 'AKTIF' and c.status_aktif='AKTIF' and b.id_tahun_akademik=?1 group by c.angkatan) as b on a.angkatan = b.angkatan order by angkatan asc", nativeQuery = true)
    List<Object[]> listAllTagihanPerAngkatanByTahunAkademik(String tahunAkademik);

    @Query(value = "select sum(coalesce(a.tagihan,0)) as tagihan from (select a.id,b.angkatan as angkatan, coalesce(a.nilai_tagihan,0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as u on b.id_prodi = u.id inner join tahun_akademik as d on a.id_tahun_akademik=d.id where a.status='AKTIF' and b.status_aktif='AKTIF')as a", nativeQuery = true)
    BigDecimal totalAllTagihan();

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan as a join tahun_akademik as b on a.id_tahun_akademik=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where a.status!='HAPUS' and c.status_aktif='AKTIF' and b.id=?1", nativeQuery = true)
    BigDecimal totalAllTagihanByTahunAkademik(String tahunAkademik);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id where b.nim=?1 and a.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalAllTagihanPerMahasiswa(String nim);

    @Query(value = "select coalesce(sum(nilai_tagihan_asli),0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id where b.nim=?1 and a.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalAllTagihanAsliPerMahasiswa(String nim);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id where b.nim=?1 and a.id_tahun_akademik=?2 and a.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalAllTagihanPerMahasiswaByTahunAkademik(String nim, String tahunAkademik);

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

    @Query(value = "select nomor,status, \n" +
            "if(status = 'LANCAR', 'Lunas dan tepat waktu', \n" +
            "if(status = 'KURANG_LANCAR', 'Lunas dan tidak tepat waktu 2 s/d 5 bulan', \n" +
            "if(status = 'TERSENDAT', 'Lunas dan tidak tepat waktu 6 bulan atau lebih',\n" +
            "if(status = 'PERHATIAN_KHUSUS', 'Belum lunas 2 s/d 5 bulan','Belum lunas 6 bulan atau lebih')))) as keterangan, count(nomor) as total,sum(tagihan) as tagihan, sum(coalesce(dibayar,0)) as dibayar, \n" +
            "sum(sisa) as sisa  from\n" +
            "(select \n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), '01',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '02', '03')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '04','05')) as nomor,\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), 'LANCAR',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'KURANG_LANCAR', 'TERSENDAT')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'PERHATIAN_KHUSUS','MACET')) as status, \n" +
            "'' as keterangan, '' as jumlah, tagihan, dibayar,tanggal, terakhir_dibayar ,sisa\n" +
            "from \n" +
            "(select a.id_mahasiswa,tagihan, tanggal,coalesce(dibayar,0) as dibayar,terakhir_dibayar, tagihan - coalesce(dibayar,0) as sisa from\n" +
            "(select id_mahasiswa,if(status_tagihan <> 'LUNAS', min(tanggal_pembuatan), max(tanggal_pembuatan)) as tanggal, id_tahun_akademik, sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id\n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' group by id_mahasiswa) as a\n" +
            "left join\n" +
            "(select id_mahasiswa, max(waktu_bayar) as terakhir_dibayar, sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "group by id_mahasiswa) as b on a.id_mahasiswa = b.id_mahasiswa) as a) as a group by nomor order by nomor", nativeQuery = true)
    List<Object[]> listNewPiutang(List<String> idTahunAkademik);

    @Query(value = "select nomor,status, \n" +
            "if(status = 'LANCAR', 'Lunas dan tepat waktu', \n" +
            "if(status = 'KURANG_LANCAR', 'Lunas dan tidak tepat waktu 2 s/d 5 bulan', \n" +
            "if(status = 'TERSENDAT', 'Lunas dan tidak tepat waktu 6 bulan atau lebih',\n" +
            "if(status = 'PERHATIAN_KHUSUS', 'Belum lunas 2 s/d 5 bulan','Belum lunas 6 bulan atau lebih')))) as keterangan, count(nomor) as total,sum(tagihan) as tagihan, sum(coalesce(dibayar,0)) as dibayar, \n" +
            "sum(sisa) as sisa  from\n" +
            "(select \n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), '01',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '02', '03')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '04','05')) as nomor,\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), 'LANCAR',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'KURANG_LANCAR', 'TERSENDAT')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'PERHATIAN_KHUSUS','MACET')) as status, \n" +
            "'' as keterangan, '' as jumlah, tagihan, dibayar,tanggal, terakhir_dibayar ,sisa\n" +
            "from \n" +
            "(select a.id_mahasiswa,tagihan, tanggal,coalesce(dibayar,0) as dibayar,terakhir_dibayar, tagihan - coalesce(dibayar,0) as sisa from\n" +
            "(select id_mahasiswa,if(status_tagihan <> 'LUNAS', min(tanggal_pembuatan), max(tanggal_pembuatan)) as tanggal, id_tahun_akademik, sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id\n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' and b.id_prodi = ?2 group by id_mahasiswa) as a\n" +
            "left join\n" +
            "(select id_mahasiswa, max(waktu_bayar) as terakhir_dibayar, sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "group by id_mahasiswa) as b on a.id_mahasiswa = b.id_mahasiswa) as a) as a group by nomor order by nomor", nativeQuery = true)
    List<Object[]> listNewPiutangDanProdi(List<String> idTahunAkademik, String idProdi);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, a.nim, a.nama, tanggal_pembuatan,date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih,TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, c.nim, c.nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih <= 1", nativeQuery = true)
    List<Object[]> detailLancar(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, c.nim, c.nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)", nativeQuery = true)
    List<Object[]> detailKurangLancar(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (2,3,4,5)", nativeQuery = true)
    List<Object[]> detailPerhatianKhusus(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)", nativeQuery = true)
    List<Object[]> detailHampirDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih in (6)", nativeQuery = true)
    List<Object[]> detailDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa where selisih > 6", nativeQuery = true)
    List<Object[]> detailSangatDiragukan(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan = 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa", nativeQuery = true)
    List<Object[]> detailTersendat(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, coalesce(tagihan,0), coalesce(pembayaran,0), coalesce(tagihan,0) - coalesce(pembayaran,0) as sisa from (select a.id_mahasiswa, nim, nama, tanggal_pembuatan, date(now())as tanggal_sekarang, waktu_bayar, TIMESTAMPDIFF(MONTH, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisih, TIMESTAMPDIFF(YEAR, tanggal_pembuatan, coalesce(waktu_bayar,NOW())) as selisihtahun, tagihan, pembayaran from (select id_mahasiswa, nim, nama, min(tanggal_pembuatan)as tanggal_pembuatan,sum(nilai_tagihan) as tagihan from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id inner join mahasiswa as c on a.id_mahasiswa=c.id where b.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status <> 'HAPUS' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null group by id_mahasiswa) a left join (select b.id_mahasiswa,sum(amount)as pembayaran, max(waktu_bayar)as waktu_bayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id inner join tahun_akademik as c on b.id_tahun_akademik = c.id where c.kode_tahun_akademik <= ?1 and a.status = 'AKTIF' and b.status = 'AKTIF' and status_tagihan <> 'LUNAS' and id_tagihan_sebelumnya is not null and c.status <> 'HAPUS' group by b.id_mahasiswa) b on a.id_mahasiswa = b.id_mahasiswa)aa", nativeQuery = true)
    List<Object[]> detailMacet(String kodeTahunAkademik, String status);

    @Query(value = "select nim, nama, tagihan, dibayar, sisa from(\n" +
            "select\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), '01',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '02', '03')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '04','05')) as nomor,\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), 'LANCAR',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'KURANG_LANCAR', 'TERSENDAT')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'PERHATIAN_KHUSUS','MACET')) as status, \n" +
            "'' as keterangan, '' as jumlah, nim, nama, tagihan, dibayar,tanggal, terakhir_dibayar ,sisa\n" +
            "from \n" +
            "(select a.id_mahasiswa, nim, nama, tagihan, tanggal,coalesce(dibayar,0) as dibayar,terakhir_dibayar, tagihan - coalesce(dibayar,0) as sisa from\n" +
            "(select id_mahasiswa, nim, nama, if(status_tagihan <> 'LUNAS', min(tanggal_pembuatan), max(tanggal_pembuatan)) as tanggal, id_tahun_akademik, sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id\n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' group by id_mahasiswa) as a\n" +
            "left join\n" +
            "(select id_mahasiswa, max(waktu_bayar) as terakhir_dibayar, sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "group by id_mahasiswa) as b on a.id_mahasiswa = b.id_mahasiswa) as a)a where status=?2", nativeQuery = true)
    List<Object[]> newDetailPiutang(List<String> idTahunAkademik, String status);

    @Query(value = "select nim, nama, tagihan, dibayar, sisa from(\n" +
            "select\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), '01',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '02', '03')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), '04','05')) as nomor,\n" +
            "if(sisa = 0, \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 1 month), 'LANCAR',\n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'KURANG_LANCAR', 'TERSENDAT')), \n" +
            "if(terakhir_dibayar <= DATE_ADD(tanggal, interval 5 month), 'PERHATIAN_KHUSUS','MACET')) as status, \n" +
            "'' as keterangan, '' as jumlah, nim, nama, tagihan, dibayar,tanggal, terakhir_dibayar ,sisa\n" +
            "from \n" +
            "(select a.id_mahasiswa, nim, nama, tagihan, tanggal,coalesce(dibayar,0) as dibayar,terakhir_dibayar, tagihan - coalesce(dibayar,0) as sisa from\n" +
            "(select id_mahasiswa, nim, nama, if(status_tagihan <> 'LUNAS', min(tanggal_pembuatan), max(tanggal_pembuatan)) as tanggal, id_tahun_akademik, sum(nilai_tagihan) as tagihan from tagihan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id\n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "and b.status = 'AKTIF' and b.id_prodi=?3 group by id_mahasiswa) as a\n" +
            "left join\n" +
            "(select id_mahasiswa, max(waktu_bayar) as terakhir_dibayar, sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan = b.id \n" +
            "where id_tahun_akademik in ?1 and a.status = 'AKTIF'\n" +
            "group by id_mahasiswa) as b on a.id_mahasiswa = b.id_mahasiswa) as a)a where status=?2", nativeQuery = true)
    List<Object[]> newDetailPiutangDanProdi(List<String> idTahunAkademik, String status, String prodi);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join " +
            "jenis_tagihan as d on c.id_jenis_tagihan=d.id where a.status='AKTIF' and d.kode='23' and a.lunas = true and a.id_mahasiswa=?1 \n" +
            "and a.id_tahun_akademik=?2 order by a.tanggal_pembuatan limit 1", nativeQuery = true)
    Tagihan tagihanSp(String idMahasiswa, String idTahun);

    @Query(value = "select e.nim, e.nama, e.email_pribadi as emailPribadi, e.email_tazkia as emailTazkia from tagihan as a inner join tahun_akademik as b on a.id_tahun_akademik=b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id inner join mahasiswa as e on a.id_mahasiswa=e.id inner join s_user as f on e.id_user=f.id inner join prodi as g on e.id_prodi=g.id where akumulasi_pembayaran = 0 and status_tagihan not in('LUNAS','DITANGGUHKAN') and a.status='AKTIF' and b.id=?1 and lunas is false and d.kode in('14','22', '40', '44') order by nim", nativeQuery = true)
    List<DisableMahasiswaDto> disableMahasiswa(TahunAkademik tahunAkademik);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndStatusAndLunas(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, StatusRecord statusRecord, boolean lunas);

    Tagihan findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord statusRecord, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, boolean lunas);

    List<Tagihan> findByMahasiswaAndNilaiJenisTagihanAndTahunAkademik(Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, TahunAkademik tahun);

    List<Tagihan> findByStatusNotInAndLunasAndMahasiswaAndTahunAkademik(List<StatusRecord> statusRecord, boolean lunas, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<Tagihan> findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    List<Tagihan> findByStatusAndStatusTagihanNotInAndMahasiswaAndLunas(StatusRecord statusRecord, List<StatusTagihan> status, Mahasiswa mahasiswa, boolean lunas);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(Mahasiswa mahasiswa, JenisTagihan jenisTagihan, TahunAkademik tahunAkademik, boolean lunas, StatusRecord statusRecord);

    Tagihan findByMahasiswaAndNilaiJenisTagihanJenisTagihanKodeInAndTahunAkademikAndStatus(Mahasiswa mahasiswa, List<String> kode, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa = b.id inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id where a.id_tahun_akademik=?1 and b.id_prodi=?2 and b.id_program=?3 and b.angkatan=?4 and a.status='AKTIF' and status_tagihan in('AKTIF', 'DITANGGUHKAN') and akumulasi_pembayaran='0' and d.kode not in('02oke','03','50') and nomor is not null order by nim", nativeQuery = true)
    List<Tagihan> generatePotongan(TahunAkademik tahunAkademik, String idProid, String idProgram, String angkatan);

    @Query(value = "select a.* from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id where a.status_tagihan='AKTIF' and lunas='0' and b.nim=?1 and a.status = 'AKTIF'", nativeQuery = true)
    List<Tagihan> cekTagihanLunas(String nim);

    Tagihan findByStatusAndStatusTagihanAndMahasiswa(StatusRecord status, StatusTagihan statusTagihan, Mahasiswa mahasiswa);

    @Query(value = "select angkatan, nim, nama, prodi, program, nilai_tagihan, coalesce(nama_beasiswa, '-') as beasiswa, status from\n" +
            "(select a.id, c.id as idMahasiswa, c.id_beasiswa, c.nim, c.nama, c.angkatan, a.nilai_tagihan, d.nama_prodi as prodi, e.nama_program as program, a.status_tagihan as status from tagihan as a inner join " +
            "nilai_jenis_tagihan as b on a.id_nilai_jenis_tagihan=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id inner join prodi as d on b.id_prodi=d.id inner join program as e on b.id_program=e.id " +
            "where a.id_tahun_akademik=?1 and b.id_prodi=?3 and b.id_program=?4 and b.angkatan=?2) as a\n" +
            "left join\n" +
            "(select a.id, a.nama_beasiswa, b.id_mahasiswa from beasiswa as a inner join mahasiswa_beasiswa as b on a.id=b.id_beasiswa where a.status='AKTIF') as b on a.idMahasiswa=b.id_mahasiswa", nativeQuery = true)
    List<Object[]> listAfterGenerate(String tahun, String angkatan, String prodi, String program);

    List<Tagihan> findByMahasiswaAndStatusAndTahunAkademikAndLunasAndStatusTagihanNotIn(Mahasiswa mahasiswa, StatusRecord status, TahunAkademik tahun, boolean lunas, List<StatusTagihan> statusTagihan);

    List<Tagihan> findByMahasiswaAndStatusNotOrderByTahunAkademikKodeTahunAkademik(Mahasiswa mahasiswa, StatusRecord status);

    List<Tagihan> findByMahasiswaAndStatusOrderByStatusTagihanDesc(Mahasiswa mahasiswa, StatusRecord status);

    List<Tagihan> findByStatusAndMahasiswaIdProdiInAndMahasiswaAngkatanInAndTahunAkademikOrderByMahasiswaNim(StatusRecord status, List<Prodi> prodis, List<String> angkatan, TahunAkademik tahunAkademik);

    // untuk list tagihan rekapan prediksi test TLC
    @Query(value = "select idPrediksi, nim, mhs as nama, angkatan, ujian_ke as ujian, jenis_test as jenis, coalesce(nilai_tagihan, 0) as nilai, coalesce(status_tagihan, '-') as status from\n" +
            "(select a.id as idPrediksi, c.id as idMhs, a.ujian_ke, a.jenis_test, a.tanggal_upload, c.nim, c.nama as mhs, c.angkatan from prediksi_test a inner join periode_test b on a.id_periode=b.id inner join mahasiswa c on a.id_mahasiswa=c.id where a.status_ujian not in('REJECTED') and b.id=?1)aa\n" +
            "left join\n" +
            "(select t.id_mahasiswa, t.nilai_tagihan, status_tagihan, jt.nama from tagihan t inner join nilai_jenis_tagihan njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan jt on njt.id_jenis_tagihan=jt.id and t.status not in('HAPUS') and jt.kode in('81','82'))bb on aa.idMhs=bb.id_mahasiswa order by nim", nativeQuery = true,
    countQuery = "select count(idPrediksi) from\n" +
            "(select a.id as idPrediksi, c.id as idMhs, a.ujian_ke, a.jenis_test, a.tanggal_upload, c.nim, c.nama as mhs, c.angkatan from prediksi_test a inner join periode_test b on a.id_periode=b.id inner join mahasiswa c on a.id_mahasiswa=c.id where a.status_ujian not in('REJECTED') and b.id=?1)aa\n" +
            "left join\n" +
            "(select t.id_mahasiswa, t.nilai_tagihan, status_tagihan, jt.nama from tagihan t inner join nilai_jenis_tagihan njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan jt on njt.id_jenis_tagihan=jt.id and t.status not in('HAPUS') and jt.kode in('81','82'))bb on aa.idMhs=bb.id_mahasiswa order by nim;")
    Page<RekapTagihanDto> rekapTagihanPrediksiTest(String idPeriode, Pageable page);

    @Query(value = "select idPrediksi, nim, mhs as nama, angkatan, ujian_ke as ujian, jenis_test as jenis, coalesce(nilai_tagihan, 0) as nilai, coalesce(status_tagihan, '-') as status from\n" +
            "(select a.id as idPrediksi, c.id as idMhs, a.ujian_ke, a.jenis_test, a.tanggal_upload, c.nim, c.nama as mhs, c.angkatan from prediksi_test a inner join periode_test b on a.id_periode=b.id inner join mahasiswa c on a.id_mahasiswa=c.id where a.status_ujian not in('REJECTED') and b.id=?1)aa\n" +
            "left join\n" +
            "(select t.id_mahasiswa, t.nilai_tagihan, status_tagihan, jt.nama from tagihan t inner join nilai_jenis_tagihan njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan jt on njt.id_jenis_tagihan=jt.id and t.status not in('HAPUS') and jt.kode in('81','82'))bb on aa.idMhs=bb.id_mahasiswa order by nim", nativeQuery = true)
    List<RekapTagihanDto> downloadRekapTagihanPrediksiTest(String idPeriode);

}
