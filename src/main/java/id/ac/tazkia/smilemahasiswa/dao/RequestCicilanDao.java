package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.jws.Oneway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RequestCicilanDao extends PagingAndSortingRepository<RequestCicilan, String> {

    Page<RequestCicilan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Page<RequestCicilan> findByStatusNotInAndTagihanOrderByTanggalJatuhTempo(List<StatusRecord> asList, Tagihan tagihan, Pageable page);

    List<RequestCicilan> findByTagihanAndStatusNotInOrderByTanggalJatuhTempo(Tagihan tagihan, List<StatusRecord> asList);

    List<RequestCicilan> findByTagihanInAndStatusCicilanOrderByTanggalJatuhTempo(List<Tagihan> tagihan, StatusCicilan    statusCicilan);
    RequestCicilan findTopByTagihanInAndStatusCicilanOrderByTanggalJatuhTempo(List<Tagihan> tagihan, StatusCicilan    statusCicilan);

    Integer countRequestCicilanByTagihanAndStatus(Tagihan tagihan, StatusRecord statusRecord);

    Page<Long> countRequestCicilanByStatus(StatusRecord statusRecord, Pageable page);

    @Query(value = "select cast(a.nilai_tagihan-sum(coalesce(b.nilai_cicilan,0)) as decimal(10,0)) as sisa from tagihan as a \n" +
            "inner join request_cicilan as b on a.id=b.id_tagihan where a.status='AKTIF' and b.status='AKTIF' and a.id=?1", nativeQuery = true)
    BigDecimal sisaCicilan(String idTagihan);

    @Query(value = "select tag.id as id, c.nama as nama, e.nama as tagihan, count(b.id) as cicilan, b.status_approve as status from tagihan as tag inner join mahasiswa as c on tag.id_mahasiswa=c.id inner join nilai_jenis_tagihan as d on tag.id_nilai_jenis_tagihan=d.id inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id left join request_cicilan as b on tag.id=b.id_tagihan where b.status='AKTIF' group by tag.id, c.nama, b.status_approve", nativeQuery = true)
    Page<Object[]> listRequestCicilan(Pageable page);

    @Query(value = "select t.id,m.nama,jt.nama as tagihan, rc.tanggal_pengajuan as Pengajuan, count(rc.id),rc.status_approve, m.nim, ta.nama_tahun_akademik from tagihan as t inner join mahasiswa as m on t.id_mahasiswa = m.id inner join nilai_jenis_tagihan as nt on nt.id = t.id_nilai_jenis_tagihan inner join jenis_tagihan as jt on nt.id_jenis_tagihan = jt.id inner join request_cicilan as rc on rc.id_tagihan = t.id inner join tahun_akademik ta on t.id_tahun_akademik=ta.id where rc.status = 'AKTIF' and rc.status_cicilan != 'EDITED' group by t.id,m.nama,rc.tanggal_pengajuan,rc.status_approve order by rc.tanggal_pengajuan desc", nativeQuery = true,
            countQuery = "select count(t.id) from tagihan as t inner join mahasiswa as m " +
                    "on t.id_mahasiswa = m.id inner join nilai_jenis_tagihan as nt on nt.id = t.id_nilai_jenis_tagihan inner join jenis_tagihan as jt " +
                    "on nt.id_jenis_tagihan = jt.id inner join request_cicilan as rc on rc.id_tagihan = t.id inner join tahun_akademik ta on t.id_tahun_akademik=ta.id where rc.status = 'AKTIF' group by t.id,m.nama,rc.tanggal_pengajuan,rc.status_approve;")
    Page<Object[]> listRequestCicilan1(Pageable page);

    @Query(value = "select t.id,m.nama,jt.nama as tagihan, rc.tanggal_pengajuan as Pengajuan, count(rc.id),rc.status_approve, m.nim, ta.nama_tahun_akademik from tagihan as t inner join mahasiswa as m on t.id_mahasiswa = m.id inner join nilai_jenis_tagihan as nt on nt.id = t.id_nilai_jenis_tagihan inner join jenis_tagihan as jt on nt.id_jenis_tagihan = jt.id inner join request_cicilan as rc on rc.id_tagihan = t.id inner join tahun_akademik ta on t.id_tahun_akademik=ta.id where m.nama like %?1% or m.nim like %?1% and rc.status = 'AKTIF' and rc.status_cicilan != 'EDITED' group by t.id,m.nama,rc.tanggal_pengajuan,rc.status_approve order by rc.tanggal_pengajuan desc", nativeQuery = true,
            countQuery = "select count(t.id) from tagihan as t inner join mahasiswa as m " +
                    "on t.id_mahasiswa = m.id inner join nilai_jenis_tagihan as nt on nt.id = t.id_nilai_jenis_tagihan inner join jenis_tagihan as jt " +
                    "on nt.id_jenis_tagihan = jt.id inner join request_cicilan as rc on rc.id_tagihan = t.id inner join tahun_akademik ta on t.id_tahun_akademik=ta.id where rc.status = 'AKTIF' " +
                    "and m.nama like %?1% or m.nim like %?1% group by t.id,m.nama,rc.tanggal_pengajuan,rc.status_approve order by rc.tanggal_pengajuan desc")
    Page<Object[]> listRequestCicilanSearch(String search, Pageable page);

    @Query(value = "select * from request_cicilan where id_tagihan = ?1 and status_cicilan = 'CICILAN' and status_approve='APPROVED' order by tanggal_jatuh_tempo limit 1", nativeQuery = true)
    RequestCicilan cariCicilanSelanjutnya(Tagihan tagihan);

    @Query(value = "select * from request_cicilan where id_tagihan=?1 and status in('AKTIF', 'NONAKTIF') and status_approve='APPROVED' limit 1", nativeQuery = true)
    RequestCicilan cariCicilan(String idTagihan);

    @Query(value = "select a.id, e.nama as tagihan, count(b.id) as cicilan, b.status_approve as status, b.status_cicilan from tagihan as a inner join mahasiswa as c " +
            "on a.id_mahasiswa=c.id inner join nilai_jenis_tagihan as d on a.id_nilai_jenis_tagihan=d.id " +
            "inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id left join request_cicilan as b on a.id=b.id_tagihan " +
            "where b.status='AKTIF' and a.id_tahun_akademik=?1 and c.nim=?2 " +
            "group by a.id, e.nama, b.status_approve", nativeQuery = true)
    List<Object[]> cekCicilanPerMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);

    @Query(value = "select nim, mahasiswa, nama_prodi, tagihan, b.tanggal_pengajuan, count(b.id) as totalCicilan, totalTagihan, dibayar, sisa from\n" +
            "(select a.id, b.nim, b.nama as mahasiswa, c.nama_prodi, e.nama as tagihan, a.nilai_tagihan as totalTagihan, a.akumulasi_pembayaran as dibayar, a.nilai_tagihan-a.akumulasi_pembayaran as sisa " +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id inner join nilai_jenis_tagihan as d on a.id_nilai_jenis_tagihan=d.id " +
            "inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id where status_tagihan='DICICIL' and a.id_tahun_akademik=?1) as a\n" +
            "left join\n" +
            "(select * from request_cicilan where status='AKTIF')as b on a.id=b.id_tagihan group by b.id_tagihan order by nim", nativeQuery = true,
    countQuery = "select count(nim) from\n" +
            "(select a.id, b.nim, b.nama as mahasiswa, c.nama_prodi, e.nama as tagihan, a.nilai_tagihan as totalTagihan, a.akumulasi_pembayaran as dibayar, a.nilai_tagihan-a.akumulasi_pembayaran as sisa " +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id inner join nilai_jenis_tagihan as d on a.id_nilai_jenis_tagihan=d.id " +
            "inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id where status_tagihan='DICICIL' and a.id_tahun_akademik=?1) as a\n" +
            "left join\n" +
            "(select * from request_cicilan where status='AKTIF')as b on a.id=b.id_tagihan group by b.id_tagihan order by nim")
    Page<Object[]> listRekapanCicilan(String idTahunAkademik, Pageable page);

    @Query(value = "select nim, mahasiswa, nama_prodi, tagihan, b.tanggal_pengajuan, count(b.id) as totalCicilan, totalTagihan, dibayar, sisa from\n" +
            "(select a.id, b.nim, b.nama as mahasiswa, c.nama_prodi, e.nama as tagihan, a.nilai_tagihan as totalTagihan, a.akumulasi_pembayaran as dibayar, a.nilai_tagihan-a.akumulasi_pembayaran as sisa " +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id inner join nilai_jenis_tagihan as d on a.id_nilai_jenis_tagihan=d.id " +
            "inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id where status_tagihan='DICICIL' and a.id_tahun_akademik=?3) as a\n" +
            "left join\n" +
            "(select * from request_cicilan where status='AKTIF')as b on a.id=b.id_tagihan where b.tanggal_pengajuan between ?1 and ?2 group by b.id_tagihan order by nim", nativeQuery = true,
    countQuery = "select count(nim) from\n" +
            "(select a.id, b.nim, b.nama as mahasiswa, c.nama_prodi, e.nama as tagihan, a.nilai_tagihan as totalTagihan, a.akumulasi_pembayaran as dibayar, a.nilai_tagihan-a.akumulasi_pembayaran as sisa " +
            "from tagihan as a inner join mahasiswa as b on a.id_mahasiswa=b.id inner join prodi as c on b.id_prodi=c.id inner join nilai_jenis_tagihan as d on a.id_nilai_jenis_tagihan=d.id " +
            "inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id where status_tagihan='DICICIL' and a.id_tahun_akademik=?3) as a\n" +
            "left join\n" +
            "(select * from request_cicilan where status='AKTIF')as b on a.id=b.id_tagihan where b.tanggal_pengajuan between ?1 and ?2 group by b.id_tagihan order by nim")
    Page<Object[]> listRekapanCicilanDate(String tglAwal, String tglAkhir, String idTahunAkademik, Pageable page);

    @Query(value = "select sum(nilai_cicilan) as sisaCicilan from request_cicilan where status_cicilan in('SEDANG_DITAGIHKAN', 'CICILAN') and id_tagihan=?1 and status='AKTIF'", nativeQuery = true)
    BigDecimal pengajuanPelunasan(String idTagihan);

    List<RequestCicilan> findByTagihanAndStatusAndStatusApprove(Tagihan tagihan, StatusRecord statusRecord, StatusApprove statusApprove);

    RequestCicilan findByTagihanAndStatusCicilanAndStatus(Tagihan tagihan, StatusCicilan sc, StatusRecord statusRecord);

    RequestCicilan findFirstByTagihanAndStatusCicilanAndStatus(Tagihan tagihan, StatusCicilan cicilan, StatusRecord status);

    // Integer countByTagihanAndStatusAndStatusCicilanNotIn(Tagihan tagihan, StatusRecord statusRecord, List<StatusCicilan> asList);

    @Query(value = "select count(id) from request_cicilan where id_tagihan=?1 and status='AKTIF' and status_cicilan not in ('LUNAS', 'LEWAT_JATUH_TEMPO')", nativeQuery = true)
    Integer jumlahCicilan(Tagihan tagihan);

    List<RequestCicilan> findByTagihanAndStatusAndStatusCicilanNotIn(Tagihan tagihan, StatusRecord statusRecord, List<StatusCicilan> asList);

    List<RequestCicilan> findByStatusAndStatusCicilanAndTanggalJatuhTempo(StatusRecord statusRecord, StatusCicilan statusCicilan, LocalDate tanggal);

    List<RequestCicilan> findByStatusAndStatusCicilanAndTagihanOrderByTanggalJatuhTempo(StatusRecord statusRecord, StatusCicilan statusCicilan, Tagihan tagihan);

    List<RequestCicilan> findByStatusAndTagihanOrderByTanggalJatuhTempo(StatusRecord statusRecord, Tagihan tagihan);

    @Query(value = "select floor(?1 / ?2)", nativeQuery = true)
    BigDecimal hitungCicilan(BigDecimal total, int jumlah);

    @Query(value = "select sum(nilai_cicilan) as total from request_cicilan where id_tagihan=?1 and status_cicilan='EDITED' and status='AKTIF'", nativeQuery = true)
    BigDecimal sumTotalCicilan(String idTagihan);

    // Query untuk scheduler reminder cicilan setiap tanggal 1 (semua yang belum lunas)
    List<RequestCicilan> findByStatusAndStatusApproveAndStatusCicilan(StatusRecord status, StatusApprove statusApprove, StatusCicilan statusCicilan);

}
