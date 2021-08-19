package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.select2.SelectDosen;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

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

    Long countDosenByStatus(StatusRecord aktif);

    Page<Dosen> findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(List<StatusRecord> asList, String search, String search1, Pageable page);

    @Query("select d from Dosen d where d.status not in(:status) and d not in (:dosen)")
    Iterable<Dosen> validasiDosen(@Param("status") List<StatusRecord> statusRecord, @Param("dosen") List<Dosen> dosen);
    List<Dosen> findByStatusNotInAndIdNotIn(List<StatusRecord> status, List<String> dosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen,coalesce((SELECT sum(mk.jumlah_sks) FROM jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id where id_tahun_akademik = ?1 and id_dosen_pengampu = d.id and j.status = 'AKTIF'),0) as total from dosen as d inner join karyawan as k on d.id_karyawan = k.id where k.status = 'AKTIF' and k.nama_karyawan like %?2%", nativeQuery = true)
    List<SelectDosen> searchPlotingDosen(TahunAkademik tahunAkademik, String namaDosen);

    @Query(value = "select d.id,k.nama_karyawan as dosen,(select sum(mk.jumlah_sks) FROM jadwal as j inner join matakuliah_kurikulum as mk on j.id_matakuliah_kurikulum = mk.id inner join dosen as d on j.id_dosen_pengampu = d.id inner join karyawan as k on d.id_karyawan = k.id where id_tahun_akademik = ?1 and d.id = ?2 and j.status = 'AKTIF') as total from dosen as d inner join karyawan as k on d.id_karyawan = k.id where d.id = ?2", nativeQuery = true)
    SelectDosen detailDosen(TahunAkademik tahunAkademik, Dosen namaDosen);
}
