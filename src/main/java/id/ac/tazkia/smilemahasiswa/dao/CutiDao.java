package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Cuti;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface CutiDao extends PagingAndSortingRepository<Cuti, String> {

    List<Cuti> findCutiByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    Cuti findCutiByStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);

    Page<Cuti> findByStatusOrderByStatusPengajuaanDesc(StatusRecord statusRecord, Pageable pageable);

    @Query(value = "select a.id, b.nim, b.nama, a.keterangan, a.tanggal_mulai_cuti, a.tanggal_berakhir_cuti, a.status_pengajuaan, a.status from cuti as a \n" +
            "\tinner join mahasiswa as b on b.id = a.id_mahasiswa\n" +
            "    inner join dosen as c on c.id = b.id_dosen_wali\n" +
            "    inner join karyawan as d on d.id = c.id_karyawan\n" +
            "    inner join s_user as e on e.id = d.id_user\n" +
            "    where e.id = ?1 and a.status = 'AKTIF'" +
            "    order by a.status_pengajuaan desc", nativeQuery = true)
    List<Object[]> listCutiDosenWali(User user);

}
