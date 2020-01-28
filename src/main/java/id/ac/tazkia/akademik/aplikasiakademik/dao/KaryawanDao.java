package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiRfidDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan,String> {

    Long countKaryawanByStatus (StatusRecord statusRecord);
    Karyawan findByIdUser(User user);

    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.ApiRfidDto(k.idAbsen,k.namaKaryawan,k.rfid,true ,'',0) from  Karyawan k where k.status = :status and k.rfid is not null")
    List<ApiRfidDto> rfidKaryawan(@Param("status")StatusRecord statusRecord);

    Page<Karyawan> findByStatusOrderByNamaKaryawan(StatusRecord statusRecord, Pageable page);

    Page<Karyawan> findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord statusRecord, String namaKaryawan, Pageable page);

}
