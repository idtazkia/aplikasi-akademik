package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.assesment.ScoreDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto;
import id.ac.tazkia.smilemahasiswa.dto.schedule.SesiDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal, String> {
    @Query("select sum (j.matakuliahKurikulum.jumlahSks)from Jadwal j where j.id in (:id)")
    Long totalSks(@Param("id")String[] id);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun and j.hari= :hari")
    List<ScheduleDto> schedule(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t, @Param("hari") Hari hari);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.PlotingDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish,j.kelas.namaKelas,j.matakuliahKurikulum.jumlahSks,j.dosen.karyawan.namaKaryawan) from Jadwal j where j.status = 'AKTIF' and j.tahunAkademik= :akademik and j.prodi = :prodi and j.hari is null and j.jamMulai is null and j.jamSelesai is null and j.kelas is not null")
    List<PlotingDto> ploting(@Param("prodi") Prodi prodi,@Param("akademik")TahunAkademik tahunAkademik);

    @Query(value = "SELECT a.sesi as sesi FROM (SELECT * FROM sesi)AS a LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_hari=?2 AND id_tahun_akademik=?1 AND id_ruangan=?3 AND STATUS='AKTIF')AS b ON a.sesi=b.sesi LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_kelas=?4 and id_tahun_akademik=?1 AND id_hari=?2 AND STATUS='AKTIF')AS c ON a.sesi=c.sesi LEFT JOIN (SELECT id,sesi FROM jadwal WHERE id_dosen_pengampu=?5 and id_tahun_akademik=?1 AND id_hari=?2 AND STATUS='AKTIF')AS d ON a.sesi=d.sesi WHERE b.id IS NULL AND c.id IS NULL group by a.sesi", nativeQuery = true)
    List<SesiDto> cariSesi(TahunAkademik tahunAkademik, Hari hari, Ruangan ruangan, Kelas kelas, Dosen dosen);

    @Query("select j from Jadwal j where j.id not in (:id) and j.tahunAkademik = :tahun and j.hari = :hari and j.ruangan = :ruangan and j.sesi= :sesi and j.status= :status")
    List<Jadwal> cariJadwal(@Param("id") List<String> id, @Param("tahun")TahunAkademik t, @Param("hari")Hari h, @Param("ruangan")Ruangan r, @Param("sesi")String sesi,@Param("status")StatusRecord statusRecord);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> assesment(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.dosen = :dosen and j.status =:id and j.tahunAkademik= :tahun order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> lecturerAssesment(@Param("dosen") Dosen dosen, @Param("id") StatusRecord statusRecord, @Param("tahun") TahunAkademik t);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.schedule.ScheduleDto(j.id,j.matakuliahKurikulum.matakuliah.namaMatakuliah,j.kelas.namaKelas,j.dosen.karyawan.namaKaryawan,j.matakuliahKurikulum.jumlahSks,j.jamMulai,j.jamSelesai,j.akses,j.ruangan.namaRuangan, j.hari.namaHari,j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish)from Jadwal j where j.prodi = :prodi and j.status not in (:id) and j.tahunAkademik= :tahun and j.matakuliahKurikulum.matakuliah.namaMatakuliah like %:search% order by j.matakuliahKurikulum.matakuliah.namaMatakuliahEnglish asc")
    List<ScheduleDto> assesmentSearch(@Param("prodi") Prodi prodi, @Param("id") List<StatusRecord> statusRecord, @Param("tahun") TahunAkademik t,@Param("search")String search);

    @Query(value = "select a.id as krs, a.id_mahasiswa as mahasiswa, b.nama,  b.nim ,coalesce(absensi_mahasiswa,0) as absmahasiswa ,coalesce(absen_dosen,0) as absdosen, coalesce(round(((absensi_mahasiswa * 100)/absen_dosen),2), 0) as absen, coalesce(round(((((absensi_mahasiswa * 100)/absen_dosen)*f.bobot_presensi)/100),2),0) as nilaiabsen, ROUND(((coalesce(sds,0) * coalesce(bobotsds,0))/100),2) as sds,a.nilai_uts as uts,a.nilai_uas as uas,a.nilai_akhir as akhir,a.grade as grade from krs_detail as a inner join mahasiswa as b on a.id_mahasiswa=b.id  left join(select count(a.id)as absensi_mahasiswa,id_krs_detail from presensi_mahasiswa as a inner join sesi_kuliah as b on a.id_sesi_kuliah=b.id inner join  presensi_dosen as c on b.id_presensi_dosen=c.id where a.status_presensi not in ('MANGKIR','TERLAMBAT') and c.id_jadwal=?1 and a.status='AKTIF' group by id_krs_detail) as c on a.id=c.id_krs_detail left join(select count(id)as absen_dosen,id_jadwal from presensi_dosen where id_jadwal=?1 and status='AKTIF') d on a.id_jadwal=d.id_jadwal  left join(select count(b.id_mahasiswa)as sds,b.id_mahasiswa,d.sds as bobotsds from presensi_mahasiswa as a inner join krs_detail as b on a.id_krs_detail=b.id  inner join jadwal as c on b.id_jadwal=c.id inner join matakuliah_kurikulum as d on c.id_matakuliah_kurikulum = d.id  inner join matakuliah as e on d.id_matakuliah = e.id where left(e.kode_matakuliah,3)='SDS' and a.status='AKTIF' and a.status_presensi not in ('MANGKIR','TERLAMBAT') and c.id_tahun_akademik=?2 group by a.id_mahasiswa)e on a.id_mahasiswa=e.id_mahasiswa inner join jadwal as f on a.id_jadwal = f.id where a.id_jadwal=?1 and a.status='AKTIF'", nativeQuery = true)
    List<ScoreDto>  scoreInput(Jadwal jadwal, TahunAkademik tahunAkademik);
}
