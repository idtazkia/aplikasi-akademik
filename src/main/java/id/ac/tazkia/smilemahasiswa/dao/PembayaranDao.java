package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarPembayaranDto;
import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.exolab.castor.types.DateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PembayaranDao extends PagingAndSortingRepository<Pembayaran, String> {

    Page<Pembayaran> findByTagihanAndStatus(Tagihan tagihan, StatusRecord statusRecord, Pageable page);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b\n" +
            "on a.id_tagihan=b.id\n" +
            "where b.id_tahun_akademik=?1 and b.id_mahasiswa=?2", nativeQuery = true)
    BigDecimal totalDibayarPerTahunDanMahasiswa(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join tahun_akademik as c on b.id_tahun_akademik=c.id where b.status!='HAPUS' and c.id=?1", nativeQuery = true)
    BigDecimal totalDibayar(TahunAkademik tahunAkademik);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id\n" +
            "where b.id_mahasiswa=?1 and b.status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalDibayarMahasiswa(String idMahasiswa);

    @Query(value = "select b.id_mahasiswa, b.id_tahun_akademik, a.waktu_bayar as tanggal, a.id as nomorBukti, a.amount as jumlah,\n" +
            "a.referensi as keterangan from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id\n" +
            "where b.id_tahun_akademik=?1 and id_mahasiswa=?2 and a.status='AKTIF'", nativeQuery = true)
    List<DaftarPembayaranDto> daftarPembayaran(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select b.id_mahasiswa, e.nama_tahun_akademik as namaTahun, d.nama as tagihan, a.waktu_bayar as tanggal,\n" +
            "a.amount as jumlah from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on \n" +
            "c.id_jenis_tagihan=d.id inner join tahun_akademik as e on b.id_tahun_akademik=e.id \n" +
            "where b.id_mahasiswa=?1 and a.status='AKTIF';", nativeQuery = true)
    List<Object[]> pembayaranMahasiswa(String idMahasiswa);

    @Query(value = "SELECT * FROM pembayaran where id_tagihan=?1 limit 1", nativeQuery = true)
    Pembayaran cekPembayaran(String idTagihan);

    Integer countAllByTagihan(Tagihan tagihan);

}
