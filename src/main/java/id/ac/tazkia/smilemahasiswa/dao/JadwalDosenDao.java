package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.machine.JadwalDosenDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDosenDao extends PagingAndSortingRepository<JadwalDosen,String> {
    JadwalDosen findByJadwalAndStatusJadwalDosen(Jadwal jadwal, StatusJadwalDosen statusJadwalDosen);
    List<JadwalDosen> findByStatusJadwalDosenAndJadwal(StatusJadwalDosen statusJadwalDosen,Jadwal jadwal);

    List<JadwalDosen> findByJadwal(Jadwal jadwal);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.machine.JadwalDosenDto(j.dosen.id,j.dosen.absen,j.dosen.karyawan.namaKaryawan, j.dosen.karyawan.rfid,j.jadwal.id,j.jadwal.jamMulai,j.jadwal.jamSelesai,0) from JadwalDosen j where j.jadwal.ruangan = :ruangan and j.jadwal.tahunAkademik = :tahunAkademik and j.jadwal.hari = :hari and j.jadwal.status = :status and j.jadwal.jamMulai between :mulai and :sampai ")
    Iterable<JadwalDosenDto> cariJadwal(@Param("tahunAkademik") TahunAkademik ta, @Param("ruangan") Ruangan r, @Param("hari") Hari hari, @Param("status")StatusRecord statusRecord, @Param("mulai")LocalTime mulai, @Param("sampai") LocalTime sampai);

    @Query("select j from JadwalDosen  j where j.dosen = :dosen and j.jadwal.tahunAkademik = :tahun and j.jadwal.hari =:hari and j.jadwal.ruangan = :ruangan and  :sampai between  subtime(j.jadwal.jamMulai,'500') and subtime(j.jadwal.jamSelesai,'600')")
    JadwalDosen cari(@Param("dosen")Dosen dosen, @Param("tahun") TahunAkademik tahunAkademik, @Param("hari")Hari hari, @Param("ruangan") Ruangan ruangan,@Param("sampai")LocalTime sampai);

    Iterable<JadwalDosen> findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(List<StatusRecord >hapus, TahunAkademik tahunAkademik, Dosen dosen);
}
