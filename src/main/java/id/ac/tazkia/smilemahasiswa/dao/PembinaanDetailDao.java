package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.NilaiAbsenSdsDto;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.JenisPembinaanMatrikulasiDetail;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PembinaanDetailDao extends PagingAndSortingRepository<JenisPembinaanMatrikulasiDetail,String> {

    List<JenisPembinaanMatrikulasiDetail> findByStatusAndTahunAkademik(StatusRecord statusRecord, TahunAkademik tahunAkademik);
    List<JenisPembinaanMatrikulasiDetail> findByJenisPembinaanMatrikulasi(String idJenis);

    @Query(value = "select a.id,a.id_jenis_pembinaan_matrikulasi,nama_pembinaan_matrikulasi_detail,nilai,keterangan,a.id_mahasiswa,a.id_tahun_akademik,\n" +
            "    a.status,b.id,nama_pembinaan,b.status\n" +
            "    from jenis_pembinaan_matrikulasi_detail as a\n" +
            "    inner join jenis_pembinaan_matrikulasi as b on a.id_jenis_pembinaan_matrikulasi = b.id\n" +
            "    where b.id =?1 and a.status = 'AKTIF'", nativeQuery = true)
    JenisPembinaanMatrikulasiDetail cariJenis(String idJenis);



}
