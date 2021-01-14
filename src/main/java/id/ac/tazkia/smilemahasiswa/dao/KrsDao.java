package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsDao extends PagingAndSortingRepository<Krs, String> {
    Krs findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik ta, StatusRecord s);

    Long countKrsByTahunAkademikAndMahasiswaStatus(TahunAkademik tahunAkademik, StatusRecord aktif);

    Long countKrsByTahunAkademikAndMahasiswaJenisKelamin(TahunAkademik tahunAkademik, JenisKelamin pria);

    Page<Krs> findByTahunAkademikAndProdiAndMahasiswaIdProgramAndMahasiswaAngkatan(TahunAkademik tahunAkademik, Prodi prodi, Program program, String angkatan, Pageable pageable);

    Krs findByTahunAkademikAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Mahasiswa mhsw, StatusRecord aktif);

    Page<Krs> findByProdiAndTahunAkademikAndStatus(Prodi prodi, TahunAkademik tahunAkademik, StatusRecord statusRecord, Pageable page);

    Krs findByTahunAkademikAndProdiAndStatus(TahunAkademik tahunAkademik, Prodi prd, StatusRecord aktif);

    @Query(value = "SELECT a.* FROM (SELECT a.id AS id_krs,b.nim,b.nama,c.nama_prodi FROM krs AS a INNER JOIN mahasiswa AS b ON a.id_mahasiswa=b.id INNER JOIN prodi AS c ON b.id_prodi=c.id WHERE a.id_tahun_akademik=?1 AND a.status='AKTIF' GROUP BY a.id_mahasiswa)a LEFT JOIN (SELECT * FROM krs_detail WHERE id_tahun_akademik=?1 AND STATUS='AKTIF' AND id_jadwal=?2 GROUP BY id_mahasiswa)b ON a.id_krs=b.id_krs WHERE b.id IS NULL", nativeQuery = true)
    List<Object[]> krsList(TahunAkademik tahun, Jadwal jadwal );



    @Query(value="select a.id, b.kode_tahun_akademik, b.nama_tahun_akademik,b.jenis from krs as a \n" +
            "inner join krs_detail as g on a.id = g.id_krs\n" +
            "inner join jadwal as h on g.id_jadwal = h.id\n" +
            "inner join matakuliah_kurikulum as i on h.id_matakuliah_kurikulum = i.id\n" +
            "inner join tahun_akademik as b on a.id_tahun_akademik = b.id\n" +
            "where a.status = 'AKTIF' and g.status='AKTIF' and i.jumlah_sks > 0 and a.id_mahasiswa = ?1 group by a.id \n" +
            "order by b.kode_tahun_akademik", nativeQuery = true)
    List<Object[]> semesterTranskript(String idMahasiswa);

    @Query(value = "select count(a.id) from krs as a inner join tahun_akademik as b on a.id_tahun_akademik = b.id where a.status = 'AKTIF' and a.id_mahasiswa = ?1 and b.jenis != 'PENDEK'", nativeQuery = true)
    Integer countSemester(String nim);

}
