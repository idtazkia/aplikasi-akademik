package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.select2.BaseRequest;
import id.ac.tazkia.smilemahasiswa.entity.Wilayah;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WilayahDao extends PagingAndSortingRepository<Wilayah, String> {
    @Query(value = "select kode as id,nama from wilayah where length(kode) < 3 and nama like %?1%", nativeQuery = true)
    List<BaseRequest> provinsi(String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 6 and length(kode) > 3 and kode like ?1 '__%' and nama like %?2%", nativeQuery = true)
    List<BaseRequest> kabupaten(String id,String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 9 and length(kode) > 6 and kode like ?1 '__%' and nama like %?2%", nativeQuery = true)
    List<BaseRequest> kecamatan(String id,String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 14 and length(kode) > 9 and kode like ?1 '_%' and nama like %?2%", nativeQuery = true)
    List<BaseRequest> desa(String id,String nama);

    Wilayah findByKode(String kode);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 3 and nama = ?1", nativeQuery = true)
    BaseRequest getProvinsi(String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 6 and length(kode) > 3 and kode like ?1 '__%' and nama = ?2", nativeQuery = true)
    BaseRequest getKabupaten(String id,String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 9 and length(kode) > 6 and kode like ?1 '__%' and nama = ?2", nativeQuery = true)
    BaseRequest getKecamatan(String id,String nama);

    @Query(value = "select kode as id,nama from wilayah where length(kode) < 14 and length(kode) > 9 and kode like ?1 '_%' and nama = ?2", nativeQuery = true)
    BaseRequest getDesa(String id,String nama);
}
