package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.api.akunting.CicilanPembayaranEmpatTahunDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.CicilanTagihanEmpatTahunDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarPembayaranDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PembayaranDao extends PagingAndSortingRepository<Pembayaran, String> {

    Page<Pembayaran> findByTagihanAndStatusOrderByWaktuBayar(Tagihan tagihan, StatusRecord statusRecord, Pageable page);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b\n" +
            "on a.id_tagihan=b.id\n" +
            "where b.id_tahun_akademik=?1 and b.id_mahasiswa=?2 and a.status='AKTIF'", nativeQuery = true)
    BigDecimal totalDibayarPerTahunDanMahasiswa(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join tahun_akademik as c on b.id_tahun_akademik=c.id where b.status!='HAPUS' and c.id=?1", nativeQuery = true)
    BigDecimal totalDibayar(TahunAkademik tahunAkademik);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join tahun_akademik as c on b.id_tahun_akademik=c.id inner join mahasiswa as d on b.id_mahasiswa=d.id where b.status!='HAPUS' and d.status_aktif='AKTIF' and c.id=?1", nativeQuery = true)
    BigDecimal totalAllDibayarByTahunAkademik(String tahunAkademik);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id\n" +
            "where b.id_mahasiswa=?1 and b.status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalDibayarMahasiswa(String idMahasiswa);

    @Query(value = "select a.id as idPembayaran, b.id as idTagihan, b.id_mahasiswa, b.id_tahun_akademik, a.waktu_bayar as tanggal, b.nomor as nomorBukti, a.amount as jumlah, d.nama as keterangan from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on c.id_jenis_tagihan=d.id where b.id_tahun_akademik=?1 and id_mahasiswa=?2 and a.status='AKTIF' order by tanggal", nativeQuery = true)
    List<DaftarPembayaranDto> daftarPembayaran(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select b.id_mahasiswa, e.nama_tahun_akademik as namaTahun, d.nama as tagihan, a.waktu_bayar as tanggal,\n" +
            "a.amount as jumlah from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d on \n" +
            "c.id_jenis_tagihan=d.id inner join tahun_akademik as e on b.id_tahun_akademik=e.id \n" +
            "where b.id_mahasiswa=?1 and a.status='AKTIF';", nativeQuery = true)
    List<Object[]> pembayaranMahasiswa(String idMahasiswa);

    @Query(value = "select p.id as idPembayaran, t.id as idTagihan, m.nim as nim, m.nama as nama, m.angkatan as angkatan, prm.nama_program as program, pr.nama_prodi as prodi, jt.nama as jenisTagihan, ta.nama_tahun_akademik as tahunAkademik, b.nama as bank, p.amount as jumlah, p.waktu_bayar as tanggalTransaksi, p.referensi as referensi from pembayaran as p inner join tagihan as t on p.id_tagihan=t.id inner join mahasiswa as m on t.id_mahasiswa=m.id inner join prodi as pr on m.id_prodi=pr.id inner join program as prm on m.id_program=prm.id inner join tahun_akademik as ta on t.id_tahun_akademik=ta.id inner join bank as b on p.id_bank=b.id inner join nilai_jenis_tagihan as njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan as jt on njt.id_jenis_tagihan=jt.id where p.waktu_bayar between :mulai and :sampai order by p.waktu_bayar", nativeQuery = true,
            countQuery = "select count(p.id) from pembayaran as p inner join tagihan as t on p.id_tagihan=t.id inner join mahasiswa as m on t.id_mahasiswa=m.id inner join tahun_akademik as ta on t.id_tahun_akademik=ta.id inner join bank as b on p.id_bank=b.id inner join nilai_jenis_tagihan as njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan as jt on njt.id_jenis_tagihan=jt.id where p.waktu_bayar between :mulai and :sampai order by p.waktu_bayar")
    Page<PembayaranDto> listPembayaran(@Param("mulai") String mulai, @Param("sampai") String selesai, Pageable page);

    @Query(value = "select p.id as idPembayaran, t.id as idTagihan, m.nim as nim, m.nama as nama, m.angkatan as angkatan, prm.nama_program as program, pr.nama_prodi as prodi, jt.nama as jenisTagihan, ta.nama_tahun_akademik as tahunAkademik, b.nama as bank, p.amount as jumlah, p.waktu_bayar as tanggalTransaksi, p.referensi as referensi from pembayaran as p inner join tagihan as t on p.id_tagihan=t.id inner join mahasiswa as m on t.id_mahasiswa=m.id inner join prodi as pr on m.id_prodi=pr.id inner join program as prm on m.id_program=prm.id inner join tahun_akademik as ta on t.id_tahun_akademik=ta.id inner join bank as b on p.id_bank=b.id inner join nilai_jenis_tagihan as njt on t.id_nilai_jenis_tagihan=njt.id inner join jenis_tagihan as jt on njt.id_jenis_tagihan=jt.id where p.waktu_bayar between :mulai and :sampai order by p.waktu_bayar", nativeQuery = true)
    List<PembayaranDto> downloadPembayaran(@Param("mulai") String mulai, @Param("sampai") String selesai);

    @Query(value = "select sum(a.amount)as dibayar from pembayaran as a inner join tagihan as b on b.id=a.id_tagihan inner join mahasiswa as c on b.id_mahasiswa = c.id where a.status = 'AKTIF' and b.status='AKTIF' and c.status = 'AKTIF' and c.status_aktif='AKTIF'", nativeQuery = true)
    BigDecimal totalAllDibayar();

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where c.nim=?1 and b.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalAllDibayarPerMahasiswa(String nim);

    @Query(value = "select coalesce(sum(amount),0) from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where c.nim=?1 and b.id_tahun_akademik=?2 and b.status!='HAPUS'", nativeQuery = true)
    BigDecimal totalAllDibayarPerMahasiswaByTahunAkademik(String nim, String tahunAkademik);

    @Query(value = "SELECT * FROM pembayaran where id_tagihan=?1 limit 1", nativeQuery = true)
    Pembayaran cekPembayaran(String idTagihan);

    Pembayaran findByStatusAndTagihan(StatusRecord statusRecord, Tagihan tagihan);

    Integer countAllByTagihan(Tagihan tagihan);

    List<Pembayaran> findByTagihanMahasiswaAndTagihanStatusNotOrderByTagihanTahunAkademikKodeTahunAkademik(Mahasiswa mahasiswa, StatusRecord statusRecord);

    List<Pembayaran> findByStatusAndZahirExport(StatusRecord status, AkuntansiExport akuntansiExport);

    @Query(value = "select * from\n" +
            "(select e.id, 'UPS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan UP HP',b.nim,' ',b.nama) as keterangan ,round((e.amount * 11)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program = 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF'\n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN') and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'UAS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan Asrama HP',b.nim,' ',b.nama) as keterangan ,round((e.amount * 41)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program = 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF'\n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'UKTS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan UKT HP ',b.nim,' ',b.nama) as keterangan ,round((e.amount * 47)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program = 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF' \n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'TAWS1-2' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan TA HP ',b.nim,' ',b.nama) as keterangan ,round((e.amount * 1)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program = 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF' \n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null) and year(e.waktu_bayar) > '2023') as a\n" +
            "order by nim,keterangan", nativeQuery = true)
    List<CicilanPembayaranEmpatTahunDto> integrasiCicilanPembayaranHP();

    @Query(value = "select * from\n" +
            "(select e.id, 'UPS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan UP HP',b.nim,' ',b.nama) as keterangan ,round((e.amount * 16)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program <> 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF'\n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN') and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'UAS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan Asrama HP',b.nim,' ',b.nama) as keterangan ,round((e.amount * 15)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program <> 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF'\n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'UKTS1-3' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan UKT HP ',b.nim,' ',b.nama) as keterangan ,round((e.amount * 67)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program <> 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF' \n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null)  and year(e.waktu_bayar) > '2023'\n" +
            "union\n" +
            "select e.id, 'TAWS1-2' as kodeJurnal, d.zahir_export as zahirExport,b.nim, b.nama,e.waktu_bayar as waktuBayar,tipe_pengiriman as tipePengiriman,concat('Pembayaran cicilan TA HP ',b.nim,' ',b.nama) as keterangan ,round((e.amount * 2)/100,2) as nilaiCicilan from mahasiswa_cicilan as a\n" +
            "inner join mahasiswa as b on a.id_mahasiswa = b.id and b.angkatan = '2022' and a.status = 'AKTIF' and b.id_program <> 'haf_si_prog' \n" +
            "inner join tagihan as c on b.id = c.id_mahasiswa and c.status = 'AKTIF' \n" +
            "inner join request_cicilan as d on c.id = d.id_tagihan and year(d.tanggal_jatuh_tempo) > '2023' and status_cicilan in ('LUNAS','CICILAN','SEDANG_DITAGIHKAN')and (d.zahir_export not in ('SENT') or d.zahir_export is null)\n" +
            "inner join pembayaran as e on c.id = e.id_tagihan and e.status = 'AKTIF' and (e.zahir_export not in ('SENT') or e.zahir_export is null) and year(e.waktu_bayar) > '2023') as a\n" +
            "order by nim,keterangan", nativeQuery = true)
    List<CicilanPembayaranEmpatTahunDto> integrasiCicilanPembayaranNonHP();

}
