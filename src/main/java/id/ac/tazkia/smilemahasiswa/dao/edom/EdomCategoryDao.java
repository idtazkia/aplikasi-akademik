package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.edom.EdomCategory;

public interface EdomCategoryDao extends PagingAndSortingRepository<EdomCategory, String> {
    @Query(value = "select * from edom_category where id not in ('1') and active = true order by id asc", nativeQuery = true)
    List<EdomCategory> findAllActiveSarpras();
}
