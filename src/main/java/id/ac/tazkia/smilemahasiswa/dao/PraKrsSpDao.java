package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.krs.KrsSpDto;
import id.ac.tazkia.smilemahasiswa.dto.krs.SpDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.hibernate.sql.Update;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.xml.ws.WebEndpoint;
import java.math.BigDecimal;
import java.util.List;

public interface PraKrsSpDao extends PagingAndSortingRepository<PraKrsSp, String> {

    @Query(value = "select id,id_matakuliah,group_concat(nama_matakuliah separator ' / ')as nama_matakuliah, \n" +
            "sum(jumlah)as jumlah, sum(lunas)as lunas,status, kode from\n" +
            "(select id, id_matakuliah, coalesce(id_matakuliah_setara, id_matakuliah) as id_matakuliah_setara, nama_matakuliah, nama_matakuliah_setara,\n" +
            "sum(jumlah) as jumlah, sum(lunas) as lunas , status, kode from\n" +
            "(select b.id, c.id as id_matakuliah, coalesce(c.id_matakuliah_setara, id_matakuliah) as id_matakuliah_setara, c.nama_matakuliah, c.nama_matakuliah_setara, " +
            "coalesce(count(a.id_mahasiswa),0)as jumlah, coalesce(count(e.id_mahasiswa),0)as lunas , \n" +
            "a.status_approve as status, c.kode_matakuliah as kode from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id \n" +
            "inner join \n" +
            "(select a.*,b.id_matakuliah_setara,c.nama_matakuliah as nama_matakuliah_setara from matakuliah as a \n" +
            "left join matakuliah_setara as b on a.id = b.id_matakuliah\n" +
            "left join matakuliah as c on b.id_matakuliah_setara = c.id)\n" +
            "as c on b.id_matakuliah=c.id \n" +
            "inner join tahun_akademik as d on a.id_tahun_akademik=d.id \n" +
            "left join \n" +
            "(select b.id_mahasiswa from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true) e on a.id_mahasiswa = e.id_mahasiswa \n" +
            "where a.status='AKTIF' and d.status!='NONAKTIF' and d.status!='HAPUS' \n" +
            "group by c.kode_matakuliah)a\n" +
            "group by nama_matakuliah)a\n" +
            "group by id_matakuliah_setara \n" +
            "order by nama_matakuliah;", nativeQuery = true)
    List<Object[]> listKrsSp();

    @Query(value = "select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar from " +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from " +
            "(select b.id_matakuliah,a.id_mahasiswa, d.nim, d.nama, e.nama_prodi,a.nomor_telepon, a.status_approve, c.kode_matakuliah, c.nama_matakuliah from pra_krs_sp as a " +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id " +
            "inner join matakuliah as c on b.id_matakuliah = c.id " +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id " +
            "inner join prodi as e on d.id_prodi = e.id " +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1)a " +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah)a\n" +
            "left join " +
            "(select b.id_mahasiswa from pembayaran as a " +
            "inner join tagihan as b on a.id_tagihan = b.id " +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id " +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id " +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa " +
            "group by a.id_mahasiswa,a.id_matakuliah", nativeQuery = true)
    List<Object[]> detailSp(TahunAkademik tahunAkademik);
    
    @Query(value = "select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar, coalesce(tanggalBayar, '-') from\n" +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from\n" +
            "(select b.id_matakuliah,a.id_mahasiswa, d.nim, d.nama, e.nama_prodi,a.nomor_telepon, c.nama_matakuliah, b.jumlah_sks from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id \n" +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id\n" +
            "inner join prodi as e on d.id_prodi = e.id\n" +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1)a\n" +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah)a\n" +
            "left join\n" +
            "(select b.id_mahasiswa, a.waktu_bayar as tanggalBayar from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa\n" +
            "group by a.id_mahasiswa,a.id_matakuliah\n" +
            "order by a.nim", nativeQuery = true)
    List<Object[]> allDetail(TahunAkademik tahunAkademik);

    @Query(value = "select a.* from (select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar from\n" +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from\n" +
            "(select b.id_matakuliah,a.id_mahasiswa, d.nim, c.kode_matakuliah, c.nama_matakuliah, a.status_approve from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id \n" +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id\n" +
            "inner join prodi as e on d.id_prodi = e.id\n" +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1 and a.status_approve='APPROVED')a\n" +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah where a.id_matakuliah = ?2 or id=?3 or a.kode_matakuliah=?4 or a.nama_matakuliah like %?5%)a\n" +
            "left join " +
            "(select b.id_mahasiswa from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa)a\n" +
            "where a.status_bayar = 'LUNAS' \n" +
            "group by a.id_mahasiswa, a.id_matakuliah ", nativeQuery = true)
    List<Object[]> listLunasSpPerMatkul(TahunAkademik tahunAkademik, String idMatkul, String id, String kode, String nama);

    @Query(value = "select a.* from (select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar from \n" +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from \n" +
            "(select b.id_matakuliah,a.id_mahasiswa, d.nim, c.kode_matakuliah, c.nama_matakuliah, a.status_approve from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id \n" +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id \n" +
            "inner join prodi as e on d.id_prodi = e.id \n" +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1 and d.jenis_kelamin=?6)a \n" +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah where a.id_matakuliah = ?2 or \n" +
            "id=?3 or a.kode_matakuliah=?4 or a.nama_matakuliah like %?5%)a \n" +
            "left join \n" +
            "(select b.id_mahasiswa from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa)a \n" +
            "where a.status_bayar = 'LUNAS' \n" +
            "group by a.id_mahasiswa, a.id_matakuliah ", nativeQuery = true)
    List<Object[]> listLunasPisahKelas(TahunAkademik tahunAkademik, String idMatkul, String id, String kode, String nama, String jk);

    @Query(value = "select id,id_matakuliah as idMatakuliah,group_concat(nama_matakuliah separator ' / ')as namaMatakuliah, \n" +
            "sum(jumlah)as jumlah, sum(lunas)as lunas,status, kode from " +
            "(select id, id_matakuliah, coalesce(id_matakuliah_setara, id_matakuliah) as id_matakuliah_setara, nama_matakuliah, nama_matakuliah_setara, \n" +
            "sum(jumlah) as jumlah, sum(lunas) as lunas , status, kode from " +
            "(select b.id, c.id as id_matakuliah, coalesce(c.id_matakuliah_setara, id_matakuliah) as id_matakuliah_setara, c.nama_matakuliah, c.nama_matakuliah_setara, \n" +
            "coalesce(count(a.id_mahasiswa),0)as jumlah, coalesce(count(e.id_mahasiswa),0)as lunas , " +
            "a.status_approve as status, c.kode_matakuliah as kode from pra_krs_sp as a " +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id " +
            "inner join " +
            "(select a.*,b.id_matakuliah_setara,c.nama_matakuliah as nama_matakuliah_setara from matakuliah as a " +
            "left join matakuliah_setara as b on a.id = b.id_matakuliah " +
            "left join matakuliah as c on b.id_matakuliah_setara = c.id)" +
            "as c on b.id_matakuliah=c.id " +
            "inner join tahun_akademik as d on a.id_tahun_akademik=d.id " +
            "left join " +
            "(select b.id_mahasiswa from pembayaran as a " +
            "inner join tagihan as b on a.id_tagihan = b.id " +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id " +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id " +
            "where d.kode = '23' and b.lunas = true) e on a.id_mahasiswa = e.id_mahasiswa " +
            "where a.status='AKTIF' and d.status!='NONAKTIF' and d.status!='HAPUS' " +
            "group by c.kode_matakuliah)a " +
            "group by nama_matakuliah)a " +
            "where id=?1 " +
            "group by id_matakuliah_setara " +
            "order by nama_matakuliah", nativeQuery = true)
    SpDto tampilPerMatkul(String id);

    @Query(value = "select b.id, c.nama_matakuliah, coalesce(count(a.id_mahasiswa),0) as jumlah from pra_krs_sp as a right join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah=c.id where b.id=?1", nativeQuery = true)
    Object[] jumlahPerMatkul(String idMatkul);

    @Query(value = "select a.id, d.nama_matakuliah as matakuliah, c.jumlah_sks as jumlahSks, a.status_approve as status, bb.status_pengembalian as pengembalian, a.id_mahasiswa as mahasiswa, a.id_tahun_akademik as tahun_akademik, c.id as idMatakuliah from pra_krs_sp as a inner join matakuliah_kurikulum as c on a.id_matakuliah_kurikulum=c.id inner join matakuliah as d on c.id_matakuliah=d.id left join (select b.id_pra_krs_sp, b.status_pengembalian from refund_sp as b where b.status='AKTIF') bb on a.id=bb.id_pra_krs_sp where a.status='AKTIF' and a.id_mahasiswa=?1", nativeQuery = true)
    List<Object[]> listSp(String idMahasiswa);

    @Query(value = "select a.* from (select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar, coalesce(tanggalBayar, '-') from \n" +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from \n" +
            "(select b.id_matakuliah,a.id_mahasiswa, d.nim, d.nama, coalesce(d.telepon_seluler, '-'), e.nama_prodi, c.kode_matakuliah, c.nama_matakuliah from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id \n" +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id \n" +
            "inner join prodi as e on d.id_prodi = e.id \n" +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1)a \n" +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah where " +
            "id=?2 or a.id_matakuliah=?3 or a.kode_matakuliah=?4 or " +
            "a.nama_matakuliah like %?5%)a \n" +
            "left join \n" +
            "(select b.id_mahasiswa, a.waktu_bayar as tanggalBayar from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa)a \n" +
            "group by a.id_mahasiswa, a.id_matakuliah", nativeQuery = true)
    List<Object[]> excelDownlaod(TahunAkademik tahun, String id, String idMatakuliah, String kode, String nama);

    List<PraKrsSp> findByStatusAndMatakuliahKurikulum(StatusRecord statusRecord, MatakuliahKurikulum matakuliahKurikulum);

    @Query(value = "select sum(jumlah_sks) from matakuliah_kurikulum where id = ?1", nativeQuery = true)
    Integer jumlahSks1(String idMatkul1);

    @Query(value = "select sum(jumlah_sks) from matakuliah_kurikulum where id in (?1, ?2)", nativeQuery = true)
    Integer jumlahSks2(String idMatkul1, String idMatkul2);

    @Query(value = "select * from pra_krs_sp where id_mahasiswa=?1 and id_tahun_akademik=?2 and status='AKTIF' limit 1", nativeQuery = true)
    PraKrsSp cariKrsSp(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    @Query(value = "select count (*) from pra_krs_sp where id_mahasiswa=?1 and id_tahun_akademik=?2 and status='AKTIF'", nativeQuery = true)
    Integer countSp(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<PraKrsSp> findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(Mahasiswa mahasiswa, StatusRecord statusRecord, StatusApprove statusApprove, TahunAkademik tahunAkademik);

    List<PraKrsSp> findByStatus(StatusRecord statusRecord);

    Integer countMahasiswaByMatakuliahKurikulumIdAndTahunAkademikAndStatus(String matakuliahKurikulum, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    List<PraKrsSp> findByStatusAndMahasiswaAndTahunAkademik(StatusRecord status, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    @Query(value = "select a.*,if(b.id_mahasiswa != '','LUNAS', 'BELUM LUNAS')as status_bayar from\n" +
            "(select a.*, coalesce(b.id_matakuliah_setara, a.id_matakuliah) as id from\n" +
            "(select b.id_matakuliah,a.id_mahasiswa, c.nama_matakuliah, d.nama, a.status_approve, c.kode_matakuliah as kode,a.id as sp from pra_krs_sp as a \n" +
            "inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum = b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah = c.id \n" +
            "inner join mahasiswa as d on a.id_mahasiswa = d.id\n" +
            "inner join prodi as e on d.id_prodi = e.id\n" +
            "where a.status = 'AKTIF' and a.id_tahun_akademik = ?1 and a.status_approve=?6)a\n" +
            "left join matakuliah_setara as b on a.id_matakuliah = b.id_matakuliah where a.id_matakuliah = ?2 or id=?3 or a.kode=?4 or a.nama_matakuliah like %?5%)a\n" +
            "left join " +
            "(select b.id_mahasiswa from pembayaran as a \n" +
            "inner join tagihan as b on a.id_tagihan = b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id \n" +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id \n" +
            "where d.kode = '23' and b.lunas = true)b on a.id_mahasiswa = b.id_mahasiswa " +
            "group by a.id_mahasiswa, a.id_matakuliah ", nativeQuery = true)
    List<KrsSpDto> cariMatakuliah(TahunAkademik tahunAkademik, String idMatkul, String id, String kode, String nama, String status);

    @Query(value = "update pra_krs_sp set status_approve = 'APPROVED', user_update = ?1 where id in(?2)", nativeQuery = true)
    Update updateSp(Karyawan karyawan, List<String> id);

    @Modifying
    @Query(value = "update pra_krs_sp set status_approve = ?3,user_update = ?1 where id in(?2)", nativeQuery = true)
    void updateStatus(Karyawan karyawan, List<String> id, String status);

    @Query(value = "update pra_krs_sp set status_approve = 'REJECTED',user_update = ?1 where id in(?2)", nativeQuery = true)
    void updateReject(Karyawan karyawan,List<String> id);

}