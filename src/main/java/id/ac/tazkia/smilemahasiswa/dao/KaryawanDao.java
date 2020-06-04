package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan, String> {
    @Query("select new id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto(k.idAbsen,k.namaKaryawan,k.rfid,true ,'',0) from  Karyawan k where k.status = :status and k.rfid is not null")
    List<ApiRfidDto> rfidKaryawan(@Param("status")StatusRecord statusRecord);

    Karyawan findByIdUser(User user);

    Page<Karyawan> findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord aktif, String search, Pageable page);

    Page<Karyawan> findByStatusOrderByNamaKaryawan(StatusRecord aktif, Pageable page);

}
