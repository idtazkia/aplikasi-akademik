package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.DetailPendaftaranKonversi;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranRequestKonversi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DetailPendaftaranKonversiDao extends PagingAndSortingRepository<DetailPendaftaranKonversi, String> {
    @Query(value = "SELECT sum(jumlah_sks) FROM detail_pendaftaran_konversi as dp inner join krs_detail as kd on dp.id_krs_detail = kd.id inner join matakuliah_kurikulum as mk on kd.id_matakuliah_kurikulum = mk.id  where id_pendaftaran = ?1 and dp.status not in ('REJECTED', 'HAPUS')",
            nativeQuery = true)
    Integer sumTotalSks(PendaftaranRequestKonversi pendaftaranRequestKonversi);
}
