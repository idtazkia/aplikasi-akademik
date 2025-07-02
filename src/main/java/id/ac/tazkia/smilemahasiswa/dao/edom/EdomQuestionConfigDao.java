package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import groovyjarjarantlr4.v4.runtime.atn.SemanticContext.AND;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestionConfig;

public interface EdomQuestionConfigDao
                extends PagingAndSortingRepository<EdomQuestionConfig, String> {

        List<EdomQuestionConfig> findByTargetTypeAndTahunAkademik(String string, TahunAkademik tahunAkademik);

        List<EdomQuestionConfig> findByTargetTypeAndTahunAkademikAndQuestionEdomCategoryOrderByQuestionCreatedTimeAsc(
                        String targetType, TahunAkademik tahunAkademik, EdomCategory edomCategory);

        @Query(value = "SELECT eqc.* " +
                        "FROM edom_question_config eqc " +
                        "JOIN edom_question eq ON eqc.id_question = eq.id " +
                        "WHERE eqc.id_tahun_akademik = ?2 " +
                        "AND eq.id_category = ?3 " +
                        "AND NOT EXISTS ( " +
                        "  SELECT 1 FROM edom_general_response eg " +
                        "  JOIN edom_evaluation_target et ON eg.id_evaluation_target = et.id " +
                        "  WHERE eg.id_question = eq.id " +
                        "    AND et.id_mahasiswa = ?1 " +
                        "    AND et.id_tahun_akademik = ?2 " +
                        ")", nativeQuery = true)
        List<EdomQuestionConfig> findUnansweredQuestionsByMahasiswaAndTahunAndCategory(
                        Mahasiswa idMahasiswa,
                        TahunAkademik idTahunAkademik,
                        EdomCategory idCategory);

        @Query("SELECT DISTINCT ta FROM TahunAkademik ta JOIN EdomQuestionConfig e ON e.tahunAkademik.id = ta.id")
        List<TahunAkademik> findDistinctTahunAkademik();

}
