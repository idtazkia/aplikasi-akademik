package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.human.DataJabatan;
import id.ac.tazkia.smilemahasiswa.dto.human.DosenAktifDto;
import id.ac.tazkia.smilemahasiswa.dto.select2.SelectDosen;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {
    @Query("select d from Dosen d where d.status not in(:status)")
    Iterable<Dosen> cariDosen(@Param("status")StatusRecord statusRecord);

    Iterable<Dosen> findByStatusNotIn(List<StatusRecord> hapus);
    Page<Dosen> findByStatusNotIn(List<StatusRecord> hapus, Pageable pageable);

    @Query(value = "SELECT c.id AS id_dosen, d.nama_karyawan AS nama, SUM(jumlah_sks)AS sks FROM jadwal AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum = b.id INNER JOIN dosen AS c ON a.id_dosen_pengampu = c.id INNER JOIN karyawan AS d ON c.id_karyawan=d.id WHERE a.id_tahun_akademik =?1 AND a.status='AKTIF' AND b.status='AKTIF' GROUP BY a.id_dosen_pengampu", nativeQuery = true)
    List<Object[]> listDosen(TahunAkademik tahunAkademik);

    Dosen findByKaryawanRfid(String rfid);

    Dosen findByKaryawan(Karyawan karyawan);
    List<Dosen> findByKaryawan(String kar);
    Dosen findByKaryawanIdUser(User user);

    List<Dosen> findByStatusAndJabatanDosen(StatusRecord statusRecord, JabatanDosen jabatanDosen);

    Long countDosenByStatus(StatusRecord aktif);

    Page<Dosen> findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(List<StatusRecord> asList, String search, String search1, Pageable page);

    @Query("select d from Dosen d where d.status not in(:status) and d not in (:dosen)")
    Iterable<Dosen> validasiDosen(@Param("status") List<StatusRecord> statusRecord, @Param("dosen") List<Dosen> dosen);
    List<Dosen> findByStatusNotInAndIdNotIn(List<StatusRecord> status, List<String> dosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen,coalesce((SELECT sum(mk.jumlah_sks) FROM jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join matakuliah as m on mk.id_matakuliah = m.id where id_tahun_akademik = ?1 and id_dosen_pengampu = d.id and m.nama_matakuliah_english not in ('Thesis') and j.status = 'AKTIF'),0) as total from dosen as d inner join karyawan as k on d.id_karyawan = k.id where k.status = 'AKTIF' and k.nama_karyawan like %?2%", nativeQuery = true)
    List<SelectDosen> searchPlotingDosen(TahunAkademik tahunAkademik, String namaDosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen,jd.maksimal_sks as total from dosen as d inner join karyawan as k on d.id_karyawan = k.id left join jabatan_dosen as jd on d.id_jabatan = jd.id where k.status = 'AKTIF' and k.nama_karyawan like %?1% and d.id_jabatan not in (?2)", nativeQuery = true)
    List<SelectDosen> searchDosenJabatan(String namaDosen, List<String> jabatanDosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen from dosen as d inner join karyawan as k on d.id_karyawan = k.id  where k.status = 'AKTIF' and k.nama_karyawan like %?1% ", nativeQuery = true)
    List<SelectDosen> searchDosen(String namaDosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen,coalesce((SELECT sum(mk.jumlah_sks) FROM jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id where id_tahun_akademik = ?1 and id_dosen_pengampu = d.id and j.status = 'AKTIF'),0) as total from dosen as d inner join karyawan as k on d.id_karyawan = k.id where k.status = 'AKTIF' and d.id not in(?3) and k.nama_karyawan like %?2%", nativeQuery = true)
    List<SelectDosen> searchPlotingTeam(TahunAkademik tahunAkademik, String namaDosen,String id);

    @Query(value = "select d.id,k.nama_karyawan as dosen,(select sum(mk.jumlah_sks) FROM jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id inner join matakuliah as m on mk.id_matakuliah = m.id where id_tahun_akademik = ?1 and d.id = ?2 and j.status = 'AKTIF' and m.nama_matakuliah_english not in ('Thesis')) as total, jd.maksimal_sks as maksimalSks from dosen as d inner join karyawan as k on d.id_karyawan = k.id left join jabatan_dosen as jd on d.id_jabatan = jd.id where d.id = ?2", nativeQuery = true)
    SelectDosen detailDosen(TahunAkademik tahunAkademik, Dosen namaDosen);

    @Query(value = "select d.id as id,k.nama_karyawan as nama from dosen as d inner join karyawan as k on d.id_karyawan = k.id inner join jabatan_dosen as jd on d.id_jabatan = jd.id\n" +
            "where d.status = 'AKTIF' and d.id_jabatan = ?1 limit  ?2 offset ?3 ", nativeQuery = true)
    List<DataJabatan> listJabatanDosen(JabatanDosen jabatanDosen,Integer limit, Integer offset);

    @Query(value = "select d.id as id,k.nama_karyawan as nama from dosen as d inner join karyawan as k on d.id_karyawan = k.id inner join jabatan_dosen as jd on d.id_jabatan = jd.id\n" +
            "where d.status = 'AKTIF' and k.nama_karyawan like %?4% and d.id_jabatan = ?1 limit  ?2 offset ?3 ", nativeQuery = true)
    List<DataJabatan> listJabatanDosenSearch(JabatanDosen jabatanDosen,Integer limit, Integer offset, String search);

    @Query(value = "select count(*) from dosen as d inner join karyawan as k on d.id_karyawan = k.id inner join jabatan_dosen as jd on d.id_jabatan = jd.id\n" +
            "where d.status = 'AKTIF' and d.id_jabatan = ?1", nativeQuery = true)
    Integer countListJabatanDosen(JabatanDosen jabatanDosen);

    @Query(value = "select count(*) from dosen as d inner join karyawan as k on d.id_karyawan = k.id inner join jabatan_dosen as jd on d.id_jabatan = jd.id\n" +
            "where d.status = 'AKTIF' and d.id_jabatan = ?1 and k.nama_karyawan like %?2% ", nativeQuery = true)
    Integer countListJabatanDosenSearch(JabatanDosen jabatanDosen, String search);

    Optional<Dosen> findByKaryawanEmailAndStatus(String email, StatusRecord statusRecord);

    @Query(value = "SELECT d.id AS id, k.nama_karyawan AS nama, k.nidn AS nidn, k.gelar AS gelar " +
            "FROM dosen AS d INNER JOIN karyawan AS k ON d.id_karyawan = k.id " +
            "WHERE d.status = 'AKTIF' AND k.status = 'AKTIF' " +
            "ORDER BY k.nama_karyawan ASC", nativeQuery = true)
    List<DosenAktifDto> findAllDosenAktif();
}
