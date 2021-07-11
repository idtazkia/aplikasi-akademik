package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.user.PrasyaratDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prasyarat;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface PrasyaratDao extends PagingAndSortingRepository<Prasyarat,String> {
    List<Prasyarat> findByMatakuliahKurikulumAndStatus(MatakuliahKurikulum matakuliahKurikulum, StatusRecord aktif);

    @Query(value = "SELECT m.kode_matakuliah as kode,m.nama_matakuliah as matakuliah, m.nama_matakuliah_english as english, p.nilai as grade,'' as status FROM prasyarat as p inner join matakuliah as m on p.id_matakuliah_pras = m.id where p.id_matakuliah_kurikulum = ?1 and p.status = 'AKTIF' order by p.nilai desc limit 1", nativeQuery = true)
    PrasyaratDto cariPrasyarat(MatakuliahKurikulum matakuliahKurikulum);

    @Query(value = "select  m.kode_matakuliah as kode,m.nama_matakuliah as matakuliah, m.nama_matakuliah_english as english, kd.bobot as grade,'LULUS' as status from krs_detail as kd inner join matakuliah_kurikulum as mk on kd.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id where kd.id_mahasiswa = ?1 and kd.status = 'AKTIF' and kd.bobot >= ?2  and m.nama_matakuliah = ?3 or kd.id_mahasiswa = ?1 and kd.status = 'AKTIF' and kd.bobot >= ?2 and nama_matakuliah_english = ?4 or kd.id_mahasiswa = ?1 and kd.bobot >= ?2 and  kd.status = 'AKTIF' and kode_matakuliah = ?5", nativeQuery = true)
    List<PrasyaratDto> validasiPras(Mahasiswa mahasiswa, BigDecimal nilai,String matkul,String english,String kode);
}
