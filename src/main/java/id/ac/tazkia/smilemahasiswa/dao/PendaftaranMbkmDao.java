package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PendaftaranMbkmDao extends PagingAndSortingRepository<PendaftaranMbkm, String> {

    PendaftaranMbkm findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik tahun, StatusRecord status);
    PendaftaranMbkm findByMahasiswaAndTahunAkademikAndStatusAndStatusApproval(Mahasiswa mahasiswa, TahunAkademik tahun, StatusRecord status, StatusApprove statusApprove);

    List<PendaftaranMbkm> findByDosenAndStatus(Dosen dosen, StatusRecord statusRecord);

    Page<PendaftaranMbkm> findByStatusAndTahunAkademikOrderByTanggalDaftar(StatusRecord status, TahunAkademik tahun, Pageable page);

    @Query(value = "select id_matakuliah_kurikulum, kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks, semester from\n" +
            "(SELECT a.*,id_matakuliahh, b.bobot\n" +
            "FROM (\n" +
            "    SELECT a.id, a.id_matakuliah_kurikulum, a.id_matakuliah, \n" +
            "           GROUP_CONCAT('(', b.id_matakuliah_setara, ')' SEPARATOR ', ') AS setara,\n" +
            "           GROUP_CONCAT('(', c.id_matakuliah, ')' SEPARATOR ', ') AS setara2, \n" +
            "           kode_matakuliah, nama_matakuliah, nama_matakuliah_english, semester, jumlah_sks\n" +
            "    FROM (\n" +
            "        SELECT a.id, b.id AS id_matakuliah_kurikulum, c.id AS id_matakuliah, \n" +
            "               kode_matakuliah, nama_matakuliah, nama_matakuliah_english, semester, jumlah_sks\n" +
            "        FROM mahasiswa AS a\n" +
            "        INNER JOIN matakuliah_kurikulum AS b ON a.id_kurikulum = b.id_kurikulum\n" +
            "        INNER JOIN matakuliah AS c ON b.id_matakuliah = c.id\n" +
            "        WHERE a.id = ?1 AND b.status = 'AKTIF' AND c.status = 'AKTIF' and jumlah_sks > 0 \n" +
            "        ORDER BY b.semester, c.kode_matakuliah\n" +
            "    ) AS a\n" +
            "    LEFT JOIN matakuliah_setara AS b ON a.id_matakuliah = b.id_matakuliah\n" +
            "    LEFT JOIN matakuliah_setara AS c ON a.id_matakuliah = c.id_matakuliah_setara\n" +
            "    GROUP BY a.id_matakuliah_kurikulum\n" +
            ") AS a\n" +
            "LEFT JOIN (\n" +
            "    SELECT a.id_matakuliah_kurikulum, b.id_matakuliah AS id_matakuliahh, max(a.bobot) as bobot\n" +
            "    FROM krs_detail AS a\n" +
            "    INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id\n" +
            "    WHERE a.status = 'AKTIF' AND a.id_mahasiswa = ?1 AND a.bobot >= 2 group by id_matakuliah\n" +
            ") AS b\n" +
            "ON a.id_matakuliah = b.id_matakuliahh OR a.setara LIKE CONCAT('%(', b.id_matakuliahh, '),%') OR a.setara2 LIKE CONCAT('%(', b.id_matakuliahh, '),%')) as a\n" +
            "where id_matakuliahh is null group by kode_matakuliah order by semester, kode_matakuliah", nativeQuery = true)
    List<Object[]> listMatkulMbkm(String idMahasiswa);

}
