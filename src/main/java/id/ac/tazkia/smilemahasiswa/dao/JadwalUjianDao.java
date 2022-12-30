package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.schedule.ListJadwalUjian;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface JadwalUjianDao extends PagingAndSortingRepository<JadwalUjian,String> {
    JadwalUjian findByJadwalAndStatusAndJenis(Jadwal jadwal, StatusRecord status, StatusRecord jenis);

    List<JadwalUjian> findByJadwalTahunAkademikAndStatusAndJenisOrderByTanggalAscJamMulaiAsc(TahunAkademik tahunAkademik, StatusRecord status, StatusRecord jenis);
    List<JadwalUjian> findByJadwalTahunAkademikAndStatusAndJenisAndTanggalOrderByJamMulaiAsc(TahunAkademik tahunAkademik, StatusRecord status, StatusRecord jenis,LocalDate tanggal);

    @Query(value = "SELECT distinct tanggal FROM jadwal_ujian as ju inner join jadwal as j on ju.id_jadwal = j.id where ju.status = 'AKTIF' and ju.jenis = ?1 and j.id_tahun_akademik = ?2 order by ju.tanggal asc;", nativeQuery = true)
    List<Date> listTanggal(String jenis, TahunAkademik tahunAkademik);

    @Query(value = "select ju.id as id, ka.nama_karyawan as dosen,m.nama_matakuliah as matkul,k.nama_kelas as kelas,r.nama_ruangan as ruangan,DATE_FORMAT(ju.tanggal, '%W, %d %M %Y') as tanggal,concat (ju.jam_mulai, ' - ', ju.jam_selesai) as waktu from jadwal_ujian as ju inner join jadwal as j on ju.id_jadwal = j.id inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id\n" +
            "inner join matakuliah as m on mk.id_matakuliah = m.id inner join ruangan as r on ju.id_ruangan = r.id inner join kelas as k on j.id_kelas = k.id \n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as ka on d.id_karyawan = ka.id\n" +
            "where j.id_tahun_akademik = ?1 and ju.status = 'AKTIF' and ju.jenis = ?2 order by ju.tanggal,ju.jam_mulai asc", nativeQuery = true)
    List<ListJadwalUjian> jadwalUjian(TahunAkademik tahunAkademik, String jenis);

    @Query(value = "select ju.id as id, ka.nama_karyawan as dosen,m.nama_matakuliah as matkul,k.nama_kelas as kelas,r.nama_ruangan as ruangan,DATE_FORMAT(ju.tanggal, '%W, %d %M %Y') as tanggal,concat (ju.jam_mulai, ' - ', ju.jam_selesai) as waktu from jadwal_ujian as ju inner join jadwal as j on ju.id_jadwal = j.id inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id\n" +
            "inner join matakuliah as m on mk.id_matakuliah = m.id inner join ruangan as r on ju.id_ruangan = r.id inner join kelas as k on j.id_kelas = k.id \n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as ka on d.id_karyawan = ka.id\n" +
            "where j.id_tahun_akademik = ?1 and ju.status = 'AKTIF' and ju.jenis = ?2 and ju.tanggal = ?3 order by ju.tanggal,ju.jam_mulai asc", nativeQuery = true)
    List<ListJadwalUjian> jadwalUjianTanggal(TahunAkademik tahunAkademik, String jenis, String tanggal);

    @Query(value = "select ju.id as id, ka.nama_karyawan as dosen,m.nama_matakuliah as matkul,k.nama_kelas as kelas,r.nama_ruangan as ruangan,DATE_FORMAT(ju.tanggal, '%W, %d %M %Y') as tanggal,concat (ju.jam_mulai, ' - ', ju.jam_selesai) as waktu from jadwal_ujian as ju inner join jadwal as j on ju.id_jadwal = j.id inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id\n" +
            "inner join matakuliah as m on mk.id_matakuliah = m.id inner join ruangan as r on ju.id_ruangan = r.id inner join kelas as k on j.id_kelas = k.id \n" +
            "inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as ka on d.id_karyawan = ka.id\n" +
            "where j.id_tahun_akademik = ?1 and ju.status = 'AKTIF' and ju.jenis = ?2 and j.id_dosen_pengampu = ?3 order by ju.tanggal,ju.jam_mulai asc", nativeQuery = true)
    List<ListJadwalUjian> jadwalUjianDosen(TahunAkademik tahunAkademik, String jenis, Dosen dosen);
}
