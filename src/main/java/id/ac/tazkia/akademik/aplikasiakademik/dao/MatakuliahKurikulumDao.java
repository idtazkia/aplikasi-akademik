package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.MatakuliahKurikulumDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Kurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.MatakuliahKurikulum;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatakuliahKurikulumDao extends PagingAndSortingRepository<MatakuliahKurikulum, String> {
     List<MatakuliahKurikulum> findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord statusRecord, Kurikulum kurikulum, Prodi prodi, Integer sesi);
     List<MatakuliahKurikulum> findByStatusAndKurikulumAndSemesterNotNull(StatusRecord statusRecord, Kurikulum kurikulum);
     Page<MatakuliahKurikulum> findByMatakuliahNamaMatakuliahContainingIgnoreCaseAndKurikulumAndStatusOrMatakuliahNamaMatakuliahEnglishContainingIgnoreCaseAndKurikulumAndStatus(String nama,Kurikulum kurikulum, StatusRecord status,String name,Kurikulum k, StatusRecord statusRecord, Pageable page);

     @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.MatakuliahKurikulumDto(mk.id,mk.matakuliah.namaMatakuliah,mk.matakuliah.namaMatakuliahEnglish) from MatakuliahKurikulum mk where mk.status= :status and mk.kurikulum= :kurikulum")
     List<MatakuliahKurikulumDto> cariMk(@Param("status") StatusRecord statusRecord, @Param("kurikulum") Kurikulum kurikulum);

     @Query("select mk.id from MatakuliahKurikulum mk where mk.status= :status and mk.kurikulum = :kurikulum and mk.konsepNote = :konsep")
     List<String> CariMatakuliahKonsep(@Param("status")StatusRecord statusRecord,@Param("kurikulum")Kurikulum kurikulum,@Param("konsep")StatusRecord konsep);
}
