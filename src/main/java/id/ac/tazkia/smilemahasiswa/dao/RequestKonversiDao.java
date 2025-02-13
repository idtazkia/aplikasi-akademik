package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.constant.Approval;
import id.ac.tazkia.smilemahasiswa.dto.konversi.GetRequestKonversi;
import id.ac.tazkia.smilemahasiswa.dto.konversi.RequestKonversiDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RequestKonversiDao extends PagingAndSortingRepository<RequestKonversi, String> {

    List<RequestKonversi> findByStatus(StatusRecord statusRecord);

    RequestKonversi findByKrsDetailAndStatusAndApproval(KrsDetail krsDetail, StatusRecord statusRecord, StatusRecord approval);

    RequestKonversi findByKrsDetailAndStatus(KrsDetail krsDetail, StatusRecord statusRecord);

    List<RequestKonversi> findByKrsDetailAndKrsDetailJadwalAndStatus(KrsDetail krsDetail, Jadwal jadwal, StatusRecord statusRecord);


    Page<RequestKonversi> findByApprovalAndStatusOrderByCreatedTimeAsc(StatusRecord statusRecord1, StatusRecord statusRecord2, Pageable pageable);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalization(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilai(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndJenisNilaiAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, String search, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord, String search, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaId(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, Mahasiswa mahasiswa, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndApprovalAndStatusAndFinalizationAndKrsDetailMahasiswaIdAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(Prodi prodi, StatusRecord statusRecord1, StatusRecord statusRecord2, StatusRecord statusRecord3, Mahasiswa mahasiswa, String search, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndStatus(Prodi prodi, StatusRecord statusRecord, Pageable page);

    Page<RequestKonversi> findByKrsDetailMahasiswaIdProdiAndStatusAndKrsDetailMahasiswaNimContainingIgnoreCaseOrderByCreatedTimeAsc(Prodi prodi, StatusRecord statusRecord, String search, Pageable page);

    @Modifying
    @Query(value = "update request_konversi set status_tenggat='Y' where status='AKTIF'", nativeQuery = true)
    void updateStatusTenggat();

    @Query(value = "SELECT rk.id as id, kd.id as idKrsDetail, pd.id as idProdi, jd.id as idJadwal, pd.nama_prodi as namaProdi,kd.id_mahasiswa as idMahasiswa,ma.nim as nim,ma.nama as nama, rk.status as status,rk.approval as approval, rk.finalization as finalization\n" +
            "FROM request_konversi as rk\n" +
            "inner join krs_detail as kd on kd.id = rk.id_krs_detail\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join prodi as pd on ma.id_prodi = pd.id\n" +
            "where ma.id_prodi = ?1 and rk.status = 'AKTIF' and rk.approval = 'APPROVED' and rk.finalization = 'N'\n" +
            "group by kd.id_mahasiswa\n" +
            "order by ma.nama ASC", countQuery = "select count(rk.id) as jml FROM request_konversi as rk\n" +
            "inner join krs_detail as kd on kd.id = rk.id_krs_detail\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join prodi as pd on ma.id_prodi = pd.id\n" +
            "where ma.id_prodi = ?1 and rk.status = 'AKTIF' and rk.approval = 'APPROVED' and rk.finalization = 'N'\n" +
            "group by kd.id_mahasiswa\n" +
            "order by ma.nama ASC", nativeQuery = true)
    Page<RequestKonversiDto> listApprovalGroup(Prodi Prodi, Pageable pageable);


    @Query(value = "SELECT rk.id as id, kd.id as idKrsDetail, pd.id as idProdi, jd.id as idJadwal, pd.nama_prodi as namaProdi,kd.id_mahasiswa as idMahasiswa,\n" +
            "ma.nim as nim,ma.nama as nama, mk.nama_matakuliah as namaMatkul,kd.grade as gradeLama, rk.jenis_nilai as jenisNilai,\n" +
            "rk.status as status,rk.approval as approval, rk.finalization as finalization\n" +
            "FROM request_konversi as rk\n" +
            "inner join krs_detail as kd on kd.id = rk.id_krs_detail\n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join prodi as pd on ma.id_prodi = pd.id\n" +
            "where ma.id_prodi = ?1 and rk.status = 'AKTIF' and rk.approval = 'APPROVED' and rk.finalization = 'N' \n" +
            "order by ma.nama ASC", countQuery = "select count(id) as jml from\n" +
            "(SELECT rk.id as id, kd.id as idKrsDetail, pd.id as idProdi,pd.nama_prodi as namaProdi,kd.id_mahasiswa as idMahasiswa,\n" +
            "ma.nim as nim,ma.nama as nama, mk.nama_matakuliah as namaMatkul,kd.grade as gradeLama, rk.jenis_nilai as jenisNilai,\n" +
            "rk.status as status,rk.approval as approval, rk.finalization as finalization\n" +
            "FROM request_konversi as rk\n" +
            "inner join krs_detail as kd on kd.id = rk.id_krs_detail\n" +
            "inner join matakuliah_kurikulum as mku on kd.id_matakuliah_kurikulum = mku.id\n" +
            "inner join matakuliah as mk on mku.id_matakuliah = mk.id\n" +
            "inner join jadwal as jd on kd.id_jadwal = jd.id\n" +
            "inner join mahasiswa as ma on kd.id_mahasiswa = ma.id\n" +
            "inner join prodi as pd on ma.id_prodi = pd.id\n" +
            "where ma.id_prodi = ?1 and rk.status = 'AKTIF' and rk.approval = 'APPROVED' and rk.finalization = 'N' \n" +
            "order by ma.nama ASC) as a", nativeQuery = true)
    Page<GetRequestKonversi> listApprovalGroupDetail(Prodi Prodi, Pageable pageable);

    @Query(value = "select m.nim,m.nama, rk.jenis_nilai as jenis, pd.nama_prodi as prodi, rk.status as status, rk.approval as approval from request_konversi as rk inner join krs_detail as kd on rk.id_krs_detail = kd.id inner join mahasiswa as m on kd.id_mahasiswa = m.id inner join prodi as pd on m.id_prodi = pd.id where rk.status = 'AKTIF' and jenis_nilai = ?1 and m.id_prodi in (?2) and approval = ?3 and (m.nim like %?4% or m.nama like %?4%) group by kd.id_mahasiswa limit ?5 offset ?6",
            countQuery = "select count(*) from request_konversi as rk inner join krs_detail as kd on rk.id_krs_detail = kd.id inner join mahasiswa as m on kd.id_mahasiswa = m.id inner join prodi as pd on m.id_prodi = pd.id where rk.status = 'AKTIF' and jenis_nilai = ?1 and m.id_prodi in (?2) and approval = ?3 and (m.nim like %?4% or m.nama like %?4%) group by kd.id_mahasiswa limit ?5 offset ?6",
            nativeQuery = true)
    List<GetRequestKonversi> listRequestKonversi(String jenisNilai, List<String> prodi, String approval, String search, Integer limit, Integer offset);


}
