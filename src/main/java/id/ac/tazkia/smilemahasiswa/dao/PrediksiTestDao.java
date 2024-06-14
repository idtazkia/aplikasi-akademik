package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.constant.StatusUjian;
import id.ac.tazkia.smilemahasiswa.dto.kusioner.PrediksiTestDto;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PrediksiTest;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PrediksiTestDao extends PagingAndSortingRepository<PrediksiTest, String> {
    @Query(value = "select count(*) from prediksi_test where id_mahasiswa = ?1 and status in ('AKTIF', 'WAITING')", nativeQuery = true)
    Integer countTotalUjian(Mahasiswa mahasiswa);

    @Query(value = "select max(ujian_ke) from prediksi_test where id_mahasiswa = ?1 and status in ('AKTIF', 'WAITING')", nativeQuery = true)
    Integer findMaxUjian(Mahasiswa mahasiswa);

        @Query(value = "SELECT pt.* FROM prediksi_test pt " +
                "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id " +
                "INNER JOIN prodi p ON m.id_prodi = p.id " +
                "WHERE pt.status not in ('HAPUS') AND pt.status_ujian = ?2 " +
                "AND (m.nama LIKE %?1% OR m.nim LIKE %?1%) " +
                "GROUP BY pt.id_mahasiswa " +
                "ORDER BY pt.ujian_ke DESC",
                countQuery = "SELECT COUNT(*) FROM (SELECT pt.id_mahasiswa FROM prediksi_test pt " +
                        "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id " +
                        "INNER JOIN prodi p ON m.id_prodi = p.id " +
                        "WHERE pt.status not in ('HAPUS') AND pt.status_ujian = ?2 " +
                        "AND (m.nama LIKE %?1% OR m.nim LIKE %?1%) " +
                        "GROUP BY pt.id_mahasiswa) AS countQuery",
                nativeQuery = true)
        Page<PrediksiTest> findByNamaOrNimAndStatusUjian(String keyword, String statusUjian, Pageable pageable);

    @Query(value = "SELECT pt.*\n" +
            "FROM prediksi_test pt\n" +
            "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id\n" +
            "INNER JOIN prodi p ON m.id_prodi = p.id\n" +
            "WHERE pt.status NOT IN ('HAPUS')\n" +
            "  AND pt.status_ujian NOT IN ('WAITING')\n" +
            "  AND (m.nama LIKE %?1% OR m.nim LIKE %?1%)\n" +
            "  AND pt.ujian_ke = (\n" +
            "    SELECT MAX(sub_pt.ujian_ke)\n" +
            "    FROM prediksi_test sub_pt\n" +
            "    WHERE sub_pt.id_mahasiswa = pt.id_mahasiswa\n" +
            "      AND sub_pt.status NOT IN ('HAPUS')\n" +
            "      AND sub_pt.status_ujian NOT IN ('WAITING')\n" +
            "  )\n" +
            "ORDER BY pt.ujian_ke DESC",
            countQuery = "SELECT count(pt.*)\n" +
                    "FROM prediksi_test pt\n" +
                    "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id\n" +
                    "INNER JOIN prodi p ON m.id_prodi = p.id\n" +
                    "WHERE pt.status NOT IN ('HAPUS')\n" +
                    "  AND pt.status_ujian NOT IN ('WAITING')\n" +
                    "  AND (m.nama LIKE %?1% OR m.nim LIKE %?1%)\n" +
                    "  AND pt.ujian_ke = (\n" +
                    "    SELECT MAX(sub_pt.ujian_ke)\n" +
                    "    FROM prediksi_test sub_pt\n" +
                    "    WHERE sub_pt.id_mahasiswa = pt.id_mahasiswa\n" +
                    "      AND sub_pt.status NOT IN ('HAPUS')\n" +
                    "      AND sub_pt.status_ujian NOT IN ('WAITING')\n" +
                    "  )\n" +
                    "ORDER BY pt.ujian_ke DESC;",
            nativeQuery = true)
    Page<PrediksiTest> findByNamaOrNimAndStatusUjianNotIn(String keyword, String statusUjian, Pageable pageable);

    @Query(value = "SELECT pt.*\n" +
            "FROM prediksi_test pt\n" +
            "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id\n" +
            "INNER JOIN prodi p ON m.id_prodi = p.id\n" +
            "WHERE pt.status NOT IN ('HAPUS')\n" +
            "  AND pt.status_ujian NOT IN ('WAITING')\n" +
            "  AND pt.ujian_ke = (\n" +
            "    SELECT MAX(sub_pt.ujian_ke)\n" +
            "    FROM prediksi_test sub_pt\n" +
            "    WHERE sub_pt.id_mahasiswa = pt.id_mahasiswa\n" +
            "      AND sub_pt.status NOT IN ('HAPUS')\n" +
            "      AND sub_pt.status_ujian NOT IN ('WAITING')\n" +
            "  )\n" +
            "ORDER BY pt.ujian_ke DESC",
            countQuery = "SELECT count(pt.*)\n" +
                    "FROM prediksi_test pt\n" +
                    "INNER JOIN mahasiswa m ON pt.id_mahasiswa = m.id\n" +
                    "INNER JOIN prodi p ON m.id_prodi = p.id\n" +
                    "WHERE pt.status NOT IN ('HAPUS')\n" +
                    "  AND pt.status_ujian NOT IN ('WAITING')\n" +
                    "  AND pt.ujian_ke = (\n" +
                    "    SELECT MAX(sub_pt.ujian_ke)\n" +
                    "    FROM prediksi_test sub_pt\n" +
                    "    WHERE sub_pt.id_mahasiswa = pt.id_mahasiswa\n" +
                    "      AND sub_pt.status NOT IN ('HAPUS')\n" +
                    "      AND sub_pt.status_ujian NOT IN ('WAITING')\n" +
                    "  )\n" +
                    "ORDER BY pt.ujian_ke DESC;",
            nativeQuery = true)
    Page<PrediksiTest> findByDistincMahasiswa( Pageable pageable);

    @Query(value = "SELECT pt.id as id,m.nim as nim,m.nama as nama,p.nama_prodi as prodi,pt.ujian_ke as ujian,pt.status_ujian as status,pt.file_bukti as file,pt.tanggal_upload as tanggal FROM prediksi_test as pt inner join mahasiswa as m on pt.id_mahasiswa = m.id inner join prodi as p on m.id_prodi = p.id where pt.status = 'AKTIF' and pt.status_ujian = ?1", nativeQuery = true)
    Page<PrediksiTestDto> list(StatusUjian statusUjian, Pageable pageable);

    Page<PrediksiTest> findByStatusNotInAndStatusUjian(List<StatusRecord> statusRecord, StatusUjian statusUjian, Pageable pageable);
    Page<PrediksiTest> findByStatusNotInAndStatusUjianNotIn(List<StatusRecord> statusRecord, List<StatusUjian> statusUjian, Pageable pageable);

    List<PrediksiTest> findByMahasiswaAndStatusNotInOrderByUjianKeAsc(Mahasiswa mahasiswa, List<StatusRecord> statusRecord);

    List<PrediksiTest> findByMahasiswaAngkatanAndStatusAndStatusUjian(String angkatan, StatusRecord status, StatusUjian sttusUjian);

    PrediksiTest findByStatusNotInAndStatusUjianInAndMahasiswa(List<StatusRecord> status, List<StatusUjian> statusUjian, Mahasiswa mahasiswa);
}
