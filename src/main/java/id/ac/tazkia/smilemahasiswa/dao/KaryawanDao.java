package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.human.KaryawanIntDto;
import id.ac.tazkia.smilemahasiswa.dto.karyawan.KaryawanDto;
import id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto;
import id.ac.tazkia.smilemahasiswa.entity.Karyawan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan, String> {
    @Query("select new id.ac.tazkia.smilemahasiswa.dto.machine.ApiRfidDto(k.idAbsen,k.namaKaryawan,k.rfid,true ,'',0) from  Karyawan k where k.status = :status and k.idAbsen is not null and k.rfid is not null")
    List<ApiRfidDto> rfidKaryawan(@Param("status")StatusRecord statusRecord);

    //Surat tugas
    @Query("SELECT k FROM Karyawan k WHERE k.idUser = :user AND k.status = 'AKTIF'")
    Karyawan findByIdUser(@Param("user") User user);

//    Karyawan findByUser(User user);

    Karyawan findByEmail(String email);

    Page<Karyawan> findByStatusAndNamaKaryawanContainingIgnoreCaseOrderByNamaKaryawan(StatusRecord aktif, String search, Pageable page);

    Page<Karyawan> findByStatusOrderByNamaKaryawan(StatusRecord aktif, Pageable page);

    List<Karyawan> findByStatusOrderByNamaKaryawan(StatusRecord aktif);

    @Query("SELECT k FROM Karyawan k WHERE k.status = :status AND (k.idUser IS NULL OR k.idUser.id IS NOT NULL) AND (k.idUser IS NULL OR k.idUser.id != '') AND (k.idUser IS NULL OR k.idUser.id != '-') ORDER BY k.namaKaryawan")
    List<Karyawan> findActiveKaryawanWithValidUser(@Param("status") StatusRecord status);

    @Query(value = "SELECT COALESCE(id_absen,0) + 1 AS id_absen FROM karyawan ORDER BY id_absen DESC LIMIT 1" , nativeQuery = true)
    Integer cariIDAbesen();

    @Query(value = "UPDATE karyawan SET id_absen= FLOOR(RAND() * (9999 - 7777) + 999)", nativeQuery = true)
    Update updateIdAbsenKaryawan();

    @Query(value = "select id as id, nik as nik, nama_karyawan as namaKaryawan, gelar as gelar, jenis_kelamin as jenisKelamin, status as status, id_user as idUser, nidn as nidn,email as email,\n" +
            "tanggal_lahir as tanggalLahir, rfid as rfid, id_absen as idAbsen, foto as foto\n" +
            "from karyawan where status = 'AKTIF'\n" +
            "order by nama_karyawan asc", nativeQuery = true)
    List<KaryawanIntDto> apiGetKaryawan();

    @Query("SELECT new id.ac.tazkia.smilemahasiswa.dto.karyawan.KaryawanDto(k.id, k.namaKaryawan, k.nik) FROM Karyawan k WHERE k.status = :status AND (k.idUser IS NULL OR k.idUser.id IS NOT NULL) AND (k.idUser IS NULL OR k.idUser.id != '') AND (k.idUser IS NULL OR k.idUser.id != '-') AND (LOWER(k.namaKaryawan) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(k.nik) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY k.namaKaryawan")
    List<KaryawanDto> searchKaryawanWithValidUser(@Param("status") StatusRecord status, @Param("search") String search);

}
