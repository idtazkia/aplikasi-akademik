package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;

public interface CutiDao extends PagingAndSortingRepository<Cuti, String> {

    List<Cuti> findCutiByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    Cuti findCutiByStatusAndMahasiswaAndTahunAkademik(StatusRecord statusRecord, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

    Page<Cuti> findByStatusOrderByStatusPengajuaanDesc(StatusRecord statusRecord, Pageable pageable);

    Page<Cuti> findByStatusAndTahunAkademikOrderByStatusPengajuaanDesc(StatusRecord statusRecord, TahunAkademik tahunAkademik, Pageable pageable);

    @Query(value = "select a.id, d.nim, d.nama, a.keterangan, a.tanggal_mulai_cuti, a.tanggal_berakhir_cuti, a.dosen_wali_approved, a.status, a.semester, a.status_pengajuaan " +
            "from cuti as a inner join dosen as b on b.id = a.id_dosen inner join karyawan as c on c.id = b.id_karyawan inner join s_user as e on e.id = c.id_user " +
            "inner join mahasiswa as d on a.id_mahasiswa=d.id where e.id = ?1 and a.status = 'AKTIF' order by a.status_pengajuaan desc", nativeQuery = true)
    List<Object[]> listCutiDosenWali(User user);

    @Query(value = "select a.id, e.nim, e.nama, a.tanggal_mulai_cuti,a.keterangan,a.tanggal_berakhir_cuti,a.status_pengajuaan, a.status, dosen_wali_approved, " +
            "a.semester from cuti as a inner join dosen as b on a.id_kps = b.id inner join karyawan as c on c.id = b.id_karyawan inner join s_user as d on d.id = c.id_user " +
            "inner join mahasiswa as e on a.id_mahasiswa=e.id where d.id = ?1 and a.status = 'AKTIF' and a.dosen_wali_approved = 'APPROVED' order by a.tanggal_pengajuaan desc", nativeQuery = true)
    List<Object[]> listCutiKps(User user);

    List<Cuti> findByStatusAndDosenWaliApprovedInAndKpsApprovedInAndStatusPengajuaanAndTanggalBerakhirCuti(StatusRecord status, List<StatusApprove> approveDosen, List<StatusApprove> approveKps, StatusApprove approve, LocalDate tglBerakhirCuti);

}
