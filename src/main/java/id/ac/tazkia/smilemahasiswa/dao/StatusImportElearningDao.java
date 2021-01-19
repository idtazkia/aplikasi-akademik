package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.StatusImportElearning;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

public interface StatusImportElearningDao extends PagingAndSortingRepository<StatusImportElearning, String> {


    @Query(value = "select max(tanggal_import) from status_import_elearning where status = 'SUKSES' order by tanggal_import DESC",nativeQuery = true)
    LocalDate terakhirData();


}
