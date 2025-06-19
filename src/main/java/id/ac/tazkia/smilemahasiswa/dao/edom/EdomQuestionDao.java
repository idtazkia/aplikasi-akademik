package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestion;

public interface EdomQuestionDao extends PagingAndSortingRepository<EdomQuestion, String> {
    Page<EdomQuestion> findByStatus(StatusRecord status, Pageable pageable);

    List<EdomQuestion> findByStatusAndEdomCategory(StatusRecord status, EdomCategory edomCategory);

    @Query(value = "SELECT \n" + //
            "    eq.* FROM \n" + //
            "    edom_question AS eq \n" + //
            "LEFT JOIN \n" + //
            "    edom_question_config AS ev \n" + //
            "    ON eq.id = ev.id_question AND ev.id_tahun_akademik = ?1\n" + //
            "WHERE \n" + //
            "    ev.id_question IS NULL and eq.id_category = '1' and eq.status = 'AKTIF'", nativeQuery = true)
    List<EdomQuestion> cariPertanyaanDosen(TahunAkademik tahunAkademik);

    @Query(value = "SELECT \n" + //
            "    eq.* FROM \n" + //
            "    edom_question AS eq \n" + //
            "LEFT JOIN \n" + //
            "    edom_question_config AS ev \n" + //
            "    ON eq.id = ev.id_question AND ev.id_tahun_akademik = ?1\n" + //
            "WHERE \n" + //
            "    ev.id_question IS NULL and eq.id_category not in ('1') and eq.status = 'AKTIF'", nativeQuery = true)
    List<EdomQuestion> cariPertanyaanSaranaPrasarana(TahunAkademik tahunAkademik);

}
