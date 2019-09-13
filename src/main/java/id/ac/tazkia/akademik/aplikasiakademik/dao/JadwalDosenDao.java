package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface JadwalDosenDao extends PagingAndSortingRepository<JadwalDosen, String> {

    static final String REKAP_JADWAL_DOSEN = "select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto(jd.dosen.id,jd.dosen.karyawan.namaKaryawan, jd.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, jd.jadwal.matakuliahKurikulum.jumlahSks, jd.jadwal.prodi.namaProdi, jd.jadwal.kelas.namaKelas, jd.jadwal.hari.namaHari, jd.jadwal.jamMulai, jd.jadwal.jamSelesai, jd.jadwal.ruangan.namaRuangan, jd.jadwal.ruangan.gedung.namaGedung) from JadwalDosen jd where jd.statusJadwalDosen = :statusJadwalDosen and jd.jadwal.tahunAkademik = :ta and jd.jadwal.status = :statusJadwal order by jd.dosen.karyawan.namaKaryawan, jd.jadwal.hari.id, jd.jadwal.jamMulai";

    @Query(REKAP_JADWAL_DOSEN)
    Page<RekapJadwalDosenDto> rekapJadwalDosen(@Param("statusJadwalDosen")StatusJadwalDosen sjd, @Param("ta")TahunAkademik ta, @Param("statusJadwal")StatusRecord statusJadwal, Pageable page);

    static final String REKAP_JADWAL_PER_DOSEN = "select new id.ac.tazkia.akademik.aplikasiakademik.dto.RekapJadwalDosenDto(jd.dosen.id,jd.dosen.karyawan.namaKaryawan, jd.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, jd.jadwal.matakuliahKurikulum.jumlahSks, jd.jadwal.prodi.namaProdi, jd.jadwal.kelas.namaKelas, jd.jadwal.hari.namaHari, jd.jadwal.jamMulai, jd.jadwal.jamSelesai, jd.jadwal.ruangan.namaRuangan, jd.jadwal.ruangan.gedung.namaGedung) from JadwalDosen jd where jd.dosen = :dosen and jd.statusJadwalDosen = :statusJadwalDosen and jd.jadwal.tahunAkademik = :ta and jd.jadwal.status = :statusJadwal order by jd.dosen.karyawan.namaKaryawan, jd.jadwal.hari.id, jd.jadwal.jamMulai";

    @Query(REKAP_JADWAL_PER_DOSEN)
    Page<RekapJadwalDosenDto> rekapJadwalPerDosen(
            @Param("dosen") Dosen dosen,
            @Param("statusJadwalDosen")StatusJadwalDosen sjd, @Param("ta")TahunAkademik ta, @Param("statusJadwal")StatusRecord statusJadwal, Pageable page);

    static final String QUERY_JADWAL_DOSEN_DTO =
            "select new id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto(j.dosen.id,j.dosen.absen,j.dosen.karyawan.namaKaryawan," +
                    "j.dosen.karyawan.rfid,j.jadwal.id,j.jadwal.jamMulai,j.jadwal.jamSelesai,0) " +
                    "from JadwalDosen j where j.jadwal.ruangan = :ruangan " +
                    "and j.jadwal.tahunAkademik = :tahunAkademik " +
                    "and j.jadwal.hari = :hari " +
                    "and j.jadwal.jamMulai between :mulai and :sampai ";

    static final String QUERY_JADWAL_DTO =
            "select new id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDosenDto(j.dosen.id,j.dosen.absen,j.dosen.karyawan.namaKaryawan," +
                    "j.dosen.karyawan.rfid,j.jadwal.id,j.jadwal.jamMulai,j.jadwal.jamSelesai,0) " +
                    "from JadwalDosen j where j.jadwal.ruangan = :ruangan " +
                    "and j.jadwal.tahunAkademik = :tahunAkademik " +
                    "and j.jadwal.hari = :hari and j.dosen.karyawan.rfid = :rfid" +
                    "and j.jadwal.jamMulai between :mulai and :sampai ";

    Iterable<JadwalDosen> findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord status, TahunAkademik tahunAkademik, Dosen dosen);
    JadwalDosen findByJadwalAndDosenAndStatusJadwalDosen(Jadwal jadwal,Dosen dosen,StatusJadwalDosen statusJadwalDosen);
    JadwalDosen findByJadwalAndStatusJadwalDosen(Jadwal jadwal,StatusJadwalDosen statusJadwalDosen);

    @Query(QUERY_JADWAL_DOSEN_DTO)
    Iterable<JadwalDosenDto> cariJadwal(@Param("tahunAkademik") TahunAkademik ta, @Param("ruangan") Ruangan r, @Param("hari") Hari hari, @Param("mulai")LocalTime mulai, @Param("sampai") LocalTime sampai);


    @Query("select j from JadwalDosen  j where j.dosen = :dosen and j.jadwal.tahunAkademik = :tahun and j.jadwal.hari =:hari and j.jadwal.ruangan = :ruangan and  :sampai between  subtime(j.jadwal.jamMulai,'500') and j.jadwal.jamSelesai")
    JadwalDosen cari(@Param("dosen")Dosen dosen, @Param("tahun") TahunAkademik tahunAkademik, @Param("hari")Hari hari, @Param("ruangan") Ruangan ruangan,@Param("sampai")LocalTime sampai);





}
