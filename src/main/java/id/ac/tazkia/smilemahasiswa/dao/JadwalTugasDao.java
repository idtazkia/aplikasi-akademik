package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.ListTugasDto;
import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.JadwalTugas;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface JadwalTugasDao extends PagingAndSortingRepository<JadwalTugas, String> {

    List<JadwalTugas> findBySesiKuliahJadwalAndStatusOrderByPertemuanKe(Jadwal jadwal, StatusRecord status);

    @Query(value = "select jt.id, jt.id_sesi_kuliah as idSesi, jt.nama_tugas as namaTugas, jt.deskripsi, jt.petunjuk_aktivitas as petunjukAktivitas, jt.file, jt.nama_file as namaFile, jt.waktu_mulai as waktuMulai, jt.tenggat_waktu as tenggatWaktu, tj.nilai, tj.waktu_pengumpulan as waktuPengumpulan, tj.masukan from jadwal_tugas jt inner join sesi_kuliah sk on jt.id_sesi_kuliah=sk.id left join jadwal_tugas_jawaban tj on jt.id=tj.id_jadwal_tugas and (tj.status='AKTIF' or tj.status is null) and (tj.id_mahasiswa=?1 or tj.id_mahasiswa is null) where jt.status='AKTIF' and sk.id_jadwal=?2 order by jt.pertemuan_ke", nativeQuery = true)
    List<ListTugasDto> listTugasMahasiswa(String idMahasiswa, String idJadwal);

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'TUGAS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobot(@Param("jadwal")Jadwal jadwal);

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'UTS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobotUTS(@Param("jadwal")Jadwal jadwal);

    @Query("select coalesce(sum(jt.bobot), 0) from JadwalTugas jt where jt.kategoriTugas = 'UAS' and jt.status = 'AKTIF' and jt.sesiKuliah.jadwal = :jadwal")
    BigDecimal totalBobotUAS(@Param("jadwal")Jadwal jadwal);


    List<JadwalTugas> findByStatusAndSesiKuliahJadwalOrderBySesiKuliahPertemuanKe(StatusRecord status, Jadwal jadwal);

}

