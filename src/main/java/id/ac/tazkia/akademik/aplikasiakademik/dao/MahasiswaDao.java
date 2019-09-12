package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiRfidDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa,String> {
    Page<Mahasiswa> findByStatusAndAngkatanAndIdProdi(StatusRecord statusRecord, String status,Prodi prodi, Pageable page);
    Iterable<Mahasiswa> findByStatusAndAngkatanAndIdProdi(StatusRecord statusRecord, String status,Prodi prodi);
    Page<Mahasiswa> findByStatusAndAngkatan(StatusRecord statusRecord, String status, Pageable page);
    Iterable<Mahasiswa> findByStatusAndAngkatan(StatusRecord statusRecord, String status);
    Page<Mahasiswa> findByStatusNotIn(StatusRecord statusRecord,Pageable page);
    List<Mahasiswa> findByStatusNotInAndUser(StatusRecord statusRecord, User user);
    Mahasiswa findByUser(User user);
    Mahasiswa findByNim(String nim);
    List<Mahasiswa> findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord status, String angkatan, Prodi prodi, Program program);

    @Query("select distinct m.angkatan from Mahasiswa m order by m.angkatan asc")
    Iterable<Mahasiswa> cariAngkatan();

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.ApiRfidDto(m.idAbsen,m.nama,m.rfid,true ,'',0) from  Mahasiswa m where m.status = :status and m.rfid is not null")
    List<ApiRfidDto> rfidMahasiswa(@Param("status")StatusRecord statusRecord);


}
