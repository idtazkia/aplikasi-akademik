package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomQuestionConfig;

public interface EdomQuestionConfigDao
        extends PagingAndSortingRepository<EdomQuestionConfig, String> {

    List<EdomQuestionConfig> findByTargetTypeAndTahunAkademik(String string, TahunAkademik tahunAkademik);

    List<EdomQuestionConfig> findByTargetTypeAndTahunAkademikAndQuestionEdomCategoryOrderByQuestionCreatedTimeAsc(
            String targetType, TahunAkademik tahunAkademik, EdomCategory edomCategory);
}
