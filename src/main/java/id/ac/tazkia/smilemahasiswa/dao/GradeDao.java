package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.GradeDto;
import id.ac.tazkia.smilemahasiswa.entity.Grade;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.math.BigDecimal;
import java.util.List;

public interface GradeDao extends PagingAndSortingRepository<Grade,String> {
    List<Grade> findByStatus(StatusRecord aktif);

    Grade findByNama(String nama);

    @Query(value = "select nama as grade,bobot from grade where atas >= ?1 and bawah <= ?1 and status = 'AKTIF'", nativeQuery = true)
    GradeDto cariGradeNilai(BigDecimal nilaiAkhir);

}
