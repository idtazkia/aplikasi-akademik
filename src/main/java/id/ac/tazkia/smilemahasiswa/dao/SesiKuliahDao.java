package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.PresensiDosen;
import id.ac.tazkia.smilemahasiswa.entity.SesiKuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SesiKuliahDao extends PagingAndSortingRepository<SesiKuliah, String> {
    List<SesiKuliah> findByJadwalAndPresensiDosenStatusOrderByWaktuMulai(Jadwal jadwal, StatusRecord aktif);

    SesiKuliah findByPresensiDosen(PresensiDosen pd);
    @Query(value = "select * from sesi_kuliah as sk inner join presensi_dosen as pd on sk.id_presensi_dosen=pd.id inner join jadwal as j on pd.id_jadwal=j.id where " +
            "j.id=?1 and date(sk.waktu_mulai)=?2 and sk.pertemuan = 'Online' or sk.pertemuan = 'Offline' order by sk.waktu_mulai", nativeQuery = true)
    SesiKuliah cariPresensiHariIni(String idJadwal, String hariIni);
}
