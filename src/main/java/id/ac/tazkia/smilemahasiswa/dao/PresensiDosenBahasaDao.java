package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import id.ac.tazkia.smilemahasiswa.dto.tlc.attendance.GetDataPresensi;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosenBahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;

public interface PresensiDosenBahasaDao extends PagingAndSortingRepository<PresensiDosenBahasa, String> {
    List<PresensiDosenBahasa> findByJadwalBahasaAndStatusOrderByPertemuanKeAsc(JadwalBahasa jadwalBahasa, StatusRecord status);

    @Query(value = "SELECT COUNT(*) AS jumlah_presensi FROM presensi_dosen_bahasa WHERE id_jadwal_bahasa = ?1 AND status = 'AKTIF'", nativeQuery = true)
    Integer totalPertertemuan(JadwalBahasa jadwalBahasa);

    @Query(value = "select pdb.id as id, pdb.id_jadwal_bahasa as jadwal,pdb.berita_acara as berita ,k.nama_karyawan as dosen,pdb.pertemuan_ke as pertemuanke, pdb.waktu_mengajar as waktu, pdb.link_pertemuan as link, pdb.pertemuan as pertemuan, pdb.jenis as jenis, djb.jam_mulai as mulai, djb.jam_selesai as selesai from presensi_dosen_bahasa as pdb inner join dosen as d on pdb.id_dosen = d.id inner join detail_jadwal_bahasa as djb on pdb.id_detail = djb.id inner join karyawan as k on d.id_karyawan = k.id\n"
            + //
            "where pdb.status = 'AKTIF' and pdb.id_jadwal_bahasa = ?1", nativeQuery = true)
    List<GetDataPresensi> getDataPresensi(JadwalBahasa jadwalBahasa);
}
