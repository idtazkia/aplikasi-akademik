package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PraKrsSpDao extends PagingAndSortingRepository<PraKrsSp, String> {

    @Query(value = "select b.id, c.nama_matakuliah, coalesce(count(a.id_mahasiswa),0)as jumlah, coalesce(count(e.id_mahasiswa),0)as lunas , a.status_approve as status from pra_krs_sp as a " +
            "inner join matakuliah_kurikulum as b on " +
            "a.id_matakuliah_kurikulum=b.id " +
            "inner join matakuliah as c on b.id_matakuliah=c.id " +
            "inner join tahun_akademik as d on a.id_tahun_akademik=d.id " +
            "left join " +
            "(select b.id_mahasiswa from pembayaran as a " +
            "inner join tagihan as b on a.id_tagihan = b.id " +
            "inner join nilai_jenis_tagihan as c on b.id_nilai_jenis_tagihan = c.id " +
            "inner join jenis_tagihan as d on c.id_jenis_tagihan = d.id " +
            "where d.kode = '23' and b.lunas = true) e on a.id_mahasiswa = e.id_mahasiswa " +
            "where a.status='AKTIF' and d.status!='NONAKTIF' and d.status!='HAPUS' group by c.kode_matakuliah", nativeQuery = true)
    List<Object[]> listKrsSp();

    @Query(value = "select b.id, c.nama_matakuliah, coalesce(count(a.id_mahasiswa),0) as jumlah from pra_krs_sp as a right join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id \n" +
            "inner join matakuliah as c on b.id_matakuliah=c.id where b.id=?1", nativeQuery = true)
    Object[] jumlahPerMatkul(String idMatkul);

    List<PraKrsSp> findByStatusAndMatakuliahKurikulum(StatusRecord statusRecord, MatakuliahKurikulum matakuliahKurikulum);

    @Query(value = "select sum(jumlah_sks) from matakuliah_kurikulum where id = ?1", nativeQuery = true)
    Integer jumlahSks1(String idMatkul1);

    @Query(value = "select sum(jumlah_sks) from matakuliah_kurikulum where id in (?1, ?2)", nativeQuery = true)
    Integer jumlahSks2(String idMatkul1, String idMatkul2);

    @Query(value = "select * from pra_krs_sp where id_mahasiswa=?1 and id_tahun_akademik=?2 and status='AKTIF' limit 1", nativeQuery = true)
    PraKrsSp cariKrsSp(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    List<PraKrsSp> findByMatakuliahKurikulumAndStatus(MatakuliahKurikulum matakuliahKurikulum, StatusRecord statusRecord);

    List<PraKrsSp> findByStatus(StatusRecord statusRecord);

    Integer countMahasiswaByMatakuliahKurikulumIdAndTahunAkademikAndStatus(String matakuliahKurikulum, TahunAkademik tahunAkademik, StatusRecord statusRecord);

//    List<PraKrsSp> findByStatus

}
