package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.ApiJadwalDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDosenDao extends PagingAndSortingRepository<JadwalDosen, String> {

    static final String QUERY_JADWAL_DOSEN_DTO =
            "select new id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto(j.dosen.id,j.dosen.absen,j.dosen.karyawan.namaKaryawan," +
                    "j.dosen.karyawan.rfid,j.jadwal.id,j.jadwal.jamMulai,j.jadwal.jamSelesai,0) " +
                    "from JadwalDosen j where j.jadwal.ruangan = :ruangan " +
                    "and j.jadwal.tahunAkademik = :tahunAkademik " +
                    "and j.jadwal.hari = :hari " +
                    "and j.jadwal.jamMulai between :mulai and :sampai ";

    Iterable<JadwalDosen> findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord status, TahunAkademik tahunAkademik, Dosen dosen);
    JadwalDosen findByJadwalAndDosenAndStatusJadwalDosen(Jadwal jadwal,Dosen dosen,StatusJadwalDosen statusJadwalDosen);
    List<JadwalDosen> findByJadwalAndStatusJadwalDosen(Jadwal jadwal, StatusJadwalDosen statusJadwalDosen);

    @Query(QUERY_JADWAL_DOSEN_DTO)
    Iterable<JadwalDosenDto> cariJadwal(@Param("tahunAkademik") TahunAkademik ta, @Param("ruangan") Ruangan r, @Param("hari") Hari hari, @Param("mulai")LocalTime mulai, @Param("sampai") LocalTime sampai);



}
