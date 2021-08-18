package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kurikulum;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MatakuliahKurikulumDao extends PagingAndSortingRepository<MatakuliahKurikulum, String> {
    List<MatakuliahKurikulum> findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(List<StatusRecord> asList, Kurikulum kurikulum, Prodi prodi, int i);

    List<MatakuliahKurikulum> findByStatusAndKurikulumAndSemesterNotNull(StatusRecord aktif, Kurikulum kurikulum);

    @Query(value = "SELECT a.nama_kelas,b.id AS id_mkkur,c.kode_matakuliah,c.nama_matakuliah,c.nama_matakuliah_english,a.id FROM kelas AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_kurikulum = b.id_kurikulum INNER JOIN matakuliah AS c ON b.id_matakuliah=c.id LEFT JOIN (SELECT aa.*,bb.id_kelas FROM mahasiswa AS aa INNER JOIN kelas_mahasiswa AS bb ON aa.id=bb.id_mahasiswa WHERE aa.status='AKTIF' AND bb.status='AKTIF' GROUP BY bb.id_kelas) d ON a.id = d.id_kelas WHERE a.status='AKTIF' and b.status = 'AKTIF' and a.id_prodi=?1 AND a.angkatan=?2 AND semester=?3 ORDER BY nama_kelas,b.id", nativeQuery = true)
    List<Object[]> plotingDosen(Prodi prodi, String kelas, Integer semester);


}
