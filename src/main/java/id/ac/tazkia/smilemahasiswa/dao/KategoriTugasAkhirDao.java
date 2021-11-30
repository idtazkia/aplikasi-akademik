package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jenis;
import id.ac.tazkia.smilemahasiswa.entity.KategoriTugasAkhir;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KategoriTugasAkhirDao extends PagingAndSortingRepository<KategoriTugasAkhir, String> {

    Page<KategoriTugasAkhir> findByProdiAndJenisStatusNotIn(Prodi prodi, List<StatusRecord> asList, Jenis jenis, Pageable page);

}
