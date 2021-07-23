package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.MatakuliahSetaraDto;
import id.ac.tazkia.smilemahasiswa.dto.setara.PilihanSetara;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahSetara;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MataKuliahSetaraDao extends PagingAndSortingRepository<MatakuliahSetara, String> {

    @Query(value = "SELECT * FROM\n" +
            "(SELECT a.id,b.kode_matakuliah as kodeMatakuliah,b.nama_matakuliah as namaMatakuliah,b.nama_matakuliah_english as namaMatakuliahEnglish  FROM matakuliah_setara AS a \n" +
            "\tINNER JOIN matakuliah AS b ON a.id_matakuliah_setara = b.id \n" +
            "\tWHERE a.status = 'AKTIF' AND a.id_matakuliah = ?1 \n" +
            "\t)aa ORDER BY kodeMatakuliah", nativeQuery = true)
    List<MatakuliahSetaraDto> listMatakuliahSetara(String idMatakuliah);

    @Query(value = "select id,id_matakuliah as matakuliah,id_matakuliah_setara as setara from matakuliah_setara where status = 'AKTIF' and id_matakuliah=?1", nativeQuery = true)
    List<PilihanSetara> pilihanSetara(Matakuliah matakuliah);


}
