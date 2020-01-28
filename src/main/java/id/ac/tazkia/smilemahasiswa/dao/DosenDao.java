package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {
    @Query("select d from Dosen d where d.status not in(:status)")
    Iterable<Dosen> cariDosen(@Param("status")StatusRecord statusRecord);

    Iterable<Dosen> findByStatusNotIn(List<StatusRecord> hapus);

    @Query(value = "SELECT c.id AS id_dosen, d.nama_karyawan AS nama, SUM(jumlah_sks)AS sks FROM jadwal AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id INNER JOIN dosen AS c ON a.id_dosen_pengampu = c.id INNER JOIN karyawan AS d ON c.id_karyawan=d.id WHERE a.id_tahun_akademik =?1 AND a.status='AKTIF' AND b.status='AKTIF' GROUP BY a.id_dosen_pengampu", nativeQuery = true)
    List<Object[]> listDosen(TahunAkademik tahunAkademik);

    Dosen findByKaryawanRfid(String rfid);

    Dosen findByKaryawan(Karyawan karyawan);

    Long countDosenByStatus(StatusRecord aktif);
}
