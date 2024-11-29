package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.tlc.presensi.GetDataPresensiMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiMahasiswaBahasa;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PresensiMahasiswaBahasaDao extends PagingAndSortingRepository<PresensiMahasiswaBahasa, String> {
    @Query(value = "SELECT \n" +
            "    mjb.id_mahasiswa AS id,\n" +
            "    m.nim AS nim,\n" +
            "    m.nama AS nama,\n" +
            "    COALESCE(pmb.status_presensi, '-') AS presensi \n" +
            "FROM \n" +
            "    mahasiswa_jadwal_bahasa mjb\n" +
            "LEFT JOIN mahasiswa m \n" +
            "    ON mjb.id_mahasiswa = m.id\n" +
            "LEFT JOIN presensi_mahasiswa_bahasa pmb \n" +
            "    ON mjb.id_mahasiswa = pmb.id_mahasiswa\n" +
            "    AND pmb.id_presensi_dosen_bahasa = ?2\n" +
            "LEFT JOIN presensi_dosen_bahasa pdb \n" +
            "    ON pmb.id_presensi_dosen_bahasa = pdb.id\n" +
            "    AND pdb.id = ?2\n" +
            "WHERE \n" +
            "    mjb.id_jadwal_bahasa = ?1", nativeQuery = true)
    List<GetDataPresensiMahasiswa> listPresensiMahasiswa(JadwalBahasa jadwalBahasa, PresensiDosenBahasa presensiDosenBahasa);

    PresensiMahasiswaBahasa findByMahasiswaAndPresensiDosenBahasa(Mahasiswa mahasiswa, PresensiDosenBahasa presensiDosenBahasa);
}
