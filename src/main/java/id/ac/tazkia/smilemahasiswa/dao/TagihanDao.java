package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.payment.BiayaMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.DaftarBiayaDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.NilaiCicilanDto;
import id.ac.tazkia.smilemahasiswa.dto.payment.SisaTagihanDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface TagihanDao extends PagingAndSortingRepository<Tagihan, String> {

    Page<Tagihan> findByStatusNotInAndMahasiswaContainingIgnoreCaseOrderByMahasiswa(List<StatusRecord> asList, String search, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaAndTahunAkademik(StatusRecord statusRecord, Mahasiswa mhs, TahunAkademik tahunAkademik, Pageable page);
    
    Page<Tagihan> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    Page<Tagihan> findByStatusAndMahasiswaOrderByNilaiJenisTagihan(StatusRecord statusRecord, Mahasiswa mhs, Pageable page);

    Tagihan findByNomor(String nomor);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) as tagihan from tagihan\n" +
            "where id_tahun_akademik=?1 and id_mahasiswa=?2 and status='AKTIF'", nativeQuery = true)
    BigDecimal totalTagihan(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select coalesce(sum(nilai_tagihan),0) from tagihan \n" +
            "where id_mahasiswa = ?1 and status = 'AKTIF'", nativeQuery = true)
    BigDecimal totalTagihanMahasiswa(String idMahasiswa);

    @Query(value = "select aa.id_mahasiswa,id_tahun_akademik,nama_tahun_akademik as namaTahun,tagihan,0 as potongan,coalesce(dibayar,0)as dibayar,0 as penarikan,tagihan-coalesce(dibayar,0)as sisa from\n" +
            "(select a.id_tahun_akademik,b.nama_tahun_akademik,id_mahasiswa,sum(nilai_tagihan)as tagihan \n" +
            "from tagihan as a inner join tahun_akademik as b\n" +
            "on a.id_tahun_akademik=b.id where id_tahun_akademik = ?1 \n" +
            "and id_mahasiswa=?2 and a.status='AKTIF')aa left join\n" +
            "(select b.id_mahasiswa,sum(amount)as dibayar from pembayaran as a inner join tagihan as b on a.id_tagihan=b.id \n" +
            "where b.id_tahun_akademik = ?1 and b.id_mahasiswa=?2 and b.status='AKTIF')bb on aa.id_mahasiswa=bb.id_mahasiswa", nativeQuery = true)
    List<SisaTagihanDto> sisaTagihanQuery(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select a.id, a.id_mahasiswa, a.id_tahun_akademik, d.nama as namaTagihan, a.nilai_tagihan as tagihan, b.amount as dibayar\n" +
            "from tagihan as a inner join nilai_jenis_tagihan as c on a.id_nilai_jenis_tagihan=c.id inner join jenis_tagihan as d\n" +
            "on c.id_jenis_tagihan=d.id left join pembayaran as b on a.id=b.id_tagihan\n" +
            "where a.id_tahun_akademik=?1 and a.id_mahasiswa=?2 and a.status='AKTIF'", nativeQuery = true)
    List<DaftarBiayaDto> daftarBiaya(String idTahunAkademik, String idMahasiswa);

    @Query(value = "select aaa.*,coalesce(bbb.dibayar,0)as dibayar,nilai_tagihan-coalesce(dibayar,0) as sisa from\n" +
            "(select a.id,b.nama as namaTagihan,c.nama_tahun_akademik as namaTahun,a.nilai_tagihan from tagihan as a \n" +
            "inner join nilai_jenis_tagihan as g on a.id_nilai_jenis_tagihan = g.id\n" +
            "inner join jenis_tagihan as b on g.id_jenis_tagihan=b.id \n" +
            "inner join tahun_akademik as c on a.id_tahun_akademik=c.id where a.id_mahasiswa=?1 and a.status='AKTIF')aaa\n" +
            "left join\n" +
            "(select sum(amount)as dibayar,aa.id_tagihan from pembayaran as aa inner join tagihan as bb on aa.id_tagihan = bb.id where \n" +
            "bb.id_mahasiswa=?1 and bb.status='AKTIF' and aa.status='AKTIf' group by id_tagihan)bbb on aaa.id=bbb.id_tagihan" , nativeQuery = true)
    List<BiayaMahasiswaDto> biayaMahasiswa(String idMahasiswa);

    @Query(value = "select a.id as idTagihan, b.id as idCicilan, a.nilai_tagihan as nilai, round(a.nilai_tagihan / b.banyak_cicilan, 0) \n" +
            "as nilaiCicilan, a.tanggal_jatuh_tempo as jatuhTempo from tagihan as a inner join request_cicilan as b \n" +
            "on a.id=b.id_tagihan where a.id=?1 and a.status='AKTIF' and b.status='AKTIF'", nativeQuery = true)
    NilaiCicilanDto pembagianNilaiCicilan(String idTagihan);

    Tagihan findByMahasiswaAndNilaiJenisTagihanAndTahunAkademikAndStatus(Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan, TahunAkademik tahunAkademik, StatusRecord statusRecord);

    Tagihan findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihan(StatusRecord statusRecord, TahunAkademik tahunAkademik, Mahasiswa mahasiswa, NilaiJenisTagihan nilaiJenisTagihan);

    Tagihan findByNilaiJenisTagihanAndStatus(NilaiJenisTagihan nilaiJenisTagihan, StatusRecord statusRecord);

}
