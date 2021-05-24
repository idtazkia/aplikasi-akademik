package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Wilayah;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WilayahDao extends PagingAndSortingRepository<Wilayah, String> {
    @Query(value = "select * from wilayah where length(kode) < 3 limit 40", nativeQuery = true)
    Object[] provinsi();

    @Query(value = "select * from wilayah where length(kode) < 6 and length(kode) > 3 and kode like ?1 '__%'", nativeQuery = true)
    List<Wilayah> kabupaten(String id);

    @Query(value = "select * from wilayah where length(kode) < 9 and length(kode) > 6 and kode like ?1 '__%';", nativeQuery = true)
    List<Wilayah> kecamatan(String id);

    @Query(value = "select * from wilayah where length(kode) < 14 and length(kode) > 9 and kode like ?1 '_%'", nativeQuery = true)
    List<Wilayah> desa(String id);

}
