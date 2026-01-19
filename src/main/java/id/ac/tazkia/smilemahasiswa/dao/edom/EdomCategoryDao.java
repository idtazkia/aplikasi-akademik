package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import groovyjarjarantlr4.v4.runtime.atn.SemanticContext.AND;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;

public interface EdomCategoryDao extends PagingAndSortingRepository<EdomCategory, String> {
        @Query(value = "select * from edom_category where id not in ('1') and active = true order by id asc", nativeQuery = true)
        List<EdomCategory> findAllActiveSarpras();

        @Query(value = "SELECT DISTINCT ec.* " +
                        "FROM edom_category ec " +
                        "JOIN edom_question eq ON eq.id_category = ec.id " +
                        "JOIN edom_question_config eqc ON eqc.id_question = eq.id " +
                        "WHERE eqc.id_tahun_akademik = ?2 " +
                        "AND ec.active = 1 AND ec.id NOT IN ('1')" +
                        "AND eq.status <> 'HAPUS' " +
                        "AND NOT EXISTS ( " +
                        "SELECT 1 FROM edom_general_response eg " +
                        "JOIN edom_evaluation_target et ON eg.id_evaluation_target = et.id " +
                        "WHERE eg.id_question = eq.id " +
                        "AND et.id_mahasiswa = ?1 " +
                        "AND et.id_tahun_akademik = ?2 " +
                        ") ORDER BY ec.id ASC", nativeQuery = true)
        List<EdomCategory> findCategoriesWithQuestionsWithoutResponsesForMahasiswaAndTahunAkademik(
                        Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
}
