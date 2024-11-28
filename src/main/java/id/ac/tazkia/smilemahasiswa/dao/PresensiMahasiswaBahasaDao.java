package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.tlc.presensi.GetDataPresensiMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiMahasiswaBahasa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PresensiMahasiswaBahasaDao extends PagingAndSortingRepository<PresensiMahasiswaBahasa, String> {
    @Query(value = "SELECT m.id,\n" +
            "       m.nim,\n" +
            "       m.nama,\n" +
            "       COALESCE(pm.status_presensi, '-') AS status_presensi\n" +
            "FROM mahasiswa_jadwal_bahasa AS mj\n" +
            "LEFT JOIN presensi_mahasiswa_bahasa AS pm ON mj.id_mahasiswa = pm.id_mahasiswa\n" +
            "INNER JOIN mahasiswa AS m ON mj.id_mahasiswa = m.id\n" +
            "WHERE mj.id_jadwal_bahasa = ?1 \n" +
            "  AND (pm.id_presensi_dosen_bahasa = ?2 OR pm.id_presensi_dosen_bahasa IS NULL)\n" +
            "  AND m.status = 'AKTIF'", nativeQuery = true)
    List<GetDataPresensiMahasiswa> listPresensiMahasiswa(JadwalBahasa jadwalBahasa, PresensiDosenBahasa presensiDosenBahasa);
}
