package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Kurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MatakuliahKurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MatakuliahKurikulumDao extends PagingAndSortingRepository<MatakuliahKurikulum, String> {
     List<MatakuliahKurikulum> findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord statusRecord, Kurikulum kurikulum, Prodi prodi, Integer sesi);
     List<MatakuliahKurikulum> findByStatusNotInAndKurikulum(StatusRecord statusRecord, Kurikulum kurikulum);
     Page<MatakuliahKurikulum> findByStatusNotInAndKurikulumAndMatakuliahNamaMatakuliahOrMatakuliahNamaMatakuliahEnglishContainingIgnoreCase(StatusRecord statusRecord, Kurikulum kurikulum, String nama, String name, Pageable page);
     List<MatakuliahKurikulum> findByStatusAndKurikulumAndSemesterNotNull(StatusRecord statusRecord, Kurikulum kurikulum);
     Page<MatakuliahKurikulum> findByMatakuliahNamaMatakuliahContainingIgnoreCaseAndKurikulumAndStatusOrMatakuliahNamaMatakuliahEnglishContainingIgnoreCaseAndKurikulumAndStatus(String nama,Kurikulum kurikulum, StatusRecord status,String name,Kurikulum k, StatusRecord statusRecord, Pageable page);
}
