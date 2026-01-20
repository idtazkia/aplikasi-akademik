package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import id.ac.tazkia.smilemahasiswa.dto.edom.RekapEdomDto;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomEvaluationTarget;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomGeneralResponse;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestion;

public interface EdomGeneralResponseDao extends PagingAndSortingRepository<EdomGeneralResponse, String> {

    /**
     * Cek apakah response sudah ada untuk kombinasi evaluationTarget, question,
     * jadwal, dan dosen.
     * Digunakan untuk menghindari data duplikat saat resume EDOM.
     */
    Optional<EdomGeneralResponse> findByEvaluationTargetAndQuestionAndJadwalAndDosen(
            EdomEvaluationTarget evaluationTarget,
            EdomQuestion question,
            Jadwal jadwal,
            Dosen dosen);

    /**
     * Cek apakah response sudah ada berdasarkan evaluationTarget dan question
     * (untuk SARPRAS).
     */
    Optional<EdomGeneralResponse> findByEvaluationTargetAndQuestion(
            EdomEvaluationTarget evaluationTarget,
            EdomQuestion question);

    @Query("select distinct d from EdomGeneralResponse r join r.evaluationTarget t join r.dosen d join d.karyawan k where r.target = 'DOSEN' and t.tahunAkademik = :tahunAkademik order by k.namaKaryawan asc")
    List<Dosen> findListDosenTahunAkademik(@Param("tahunAkademik") TahunAkademik tahunAkademik);

    @Query(value = "SELECT " +
            "r.id_dosen AS idDosen, " +
            "k.nama_karyawan AS namaDosen, " +
            "j.id AS idJadwal, " +
            "m.nama_matakuliah AS namaMatakuliah, " +
            "COUNT(DISTINCT aktif.id_mahasiswa) AS totalMahasiswa, " +
            "COUNT(DISTINCT r.id_krs_detail) AS totalMengisi, " +
            "ROUND(AVG(sub.nilaiRataRataMahasiswa), 2) AS rataRata " +
            "FROM ( " +
            "SELECT " +
            "r.id_krs_detail, " +
            "SUM(CAST(r.response_value AS DECIMAL(10,2))) / COUNT(r.id_question) AS nilaiRataRataMahasiswa " +
            "FROM edom_general_response r " +
            "JOIN edom_evaluation_target t ON r.id_evaluation_target = t.id " +
            "WHERE " +
            "t.id_tahun_akademik = :idTahunAkademik " +
            "AND t.status = 'COMPLETED' " +
            "AND r.target = 'DOSEN' " +
            "GROUP BY r.id_krs_detail " +
            ") AS sub " +
            "JOIN edom_general_response r ON r.id_krs_detail = sub.id_krs_detail " +
            "JOIN edom_evaluation_target t ON r.id_evaluation_target = t.id " +
            "JOIN jadwal j ON r.id_jadwal = j.id " +
            "JOIN matakuliah_kurikulum mk ON j.id_matakuliah_kurikulum = mk.id " +
            "JOIN matakuliah m ON mk.id_matakuliah = m.id " +
            "LEFT JOIN krs_detail aktif ON aktif.id_jadwal = j.id AND aktif.status = 'AKTIF' " +
            "JOIN dosen d ON r.id_dosen = d.id " +
            "JOIN karyawan k ON d.id_karyawan = k.id " +
            "WHERE " +
            "t.id_tahun_akademik = :idTahunAkademik " +
            "AND t.status = 'COMPLETED' " +
            "AND r.target = 'DOSEN' " +
            "GROUP BY " +
            "r.id_dosen, k.nama_karyawan, j.id, m.nama_matakuliah " +
            "ORDER BY " +
            "k.nama_karyawan, m.nama_matakuliah", nativeQuery = true)
    List<RekapEdomDto> findRekapEdomGlobal(@Param("idTahunAkademik") String idTahunAkademik);

}
