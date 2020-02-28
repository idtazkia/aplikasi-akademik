package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa,String> {
    Mahasiswa findByUser(User user);

    @Query("select distinct m.angkatan from Mahasiswa m order by m.angkatan asc")
    Iterable<Mahasiswa> cariAngkatan();

    Mahasiswa findByNim(String nim);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto(m.idAbsen,m.nama,m.rfid,true ,'',0) from  Mahasiswa m where m.status = :status and m.rfid is not null")
    List<ApiRfidDto> rfidMahasiswa(@Param("status")StatusRecord statusRecord);

    @Query("select m.id from Mahasiswa m where m.nim = :nim")
    String cariIdMahasiswa(@Param("nim")String nim);

    @Query("select m from Mahasiswa m where m.status = :status and m.angkatan = :angkatan and m.idProdi = :prodi")
    Iterable<Mahasiswa> carikelas(@Param("status")StatusRecord statusRecord,@Param("angkatan") String angkatan,@Param("prodi") Prodi prodi);

    List<Mahasiswa> findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord aktif, String angkatan, Prodi prodi, Program program);
    List<Mahasiswa> findByStatusAndAngkatan(StatusRecord aktif,String angkatan);
}
