package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface SeminarDao extends PagingAndSortingRepository<Seminar, String> {
    Seminar findByNoteAndStatusNotIn(Note note, List<StatusApprove> statusRecords);
    Seminar findByNoteAndStatus(Note note, StatusApprove statusRecords);
    Page<Seminar> findByTahunAkademikAndNoteMahasiswaIdProdiAndStatusNotInOrderByStatusDescTanggalInputAsc(TahunAkademik tahunAkademik, Prodi prodi, List<StatusApprove> status, Pageable pageable);
    @Query(value = "select 'Jadwal_kuliah' as jenis, c.nama_matakuliah as keterangan, e.nama_karyawan as milik  from jadwal as a inner join matakuliah_kurikulum as b on a.id_matakuliah_kurikulum=b.id inner join matakuliah as c on b.id_matakuliah=c.id inner join dosen as d on a.id_dosen_pengampu=d.id inner join karyawan as e on d.id_karyawan=e.id where id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF' and jam_mulai > ?4 and jam_mulai < ?5  or id_tahun_akademik=?1 and id_hari = ?2 and id_ruangan=?3 and a.status='AKTIF'  and jam_selesai > ?4 and jam_selesai < ?5 union select 'Seminar' as jenis, b.judul as keterangan, c.nama as milik from seminar as a inner join note as b on a.id_note=b.id inner join mahasiswa as c on b.id_mahasiswa=c.id where a.status='APPROVED' and tanggal_ujian=?6 and jam_mulai > ?4 and jam_mulai < ?5 or a.status='APPROVED' and tanggal_ujian=?6 and jam_selesai > ?4 and jam_selesai < ?5 limit ?7", nativeQuery = true)
    Object[] validasiJadwalSeminar(TahunAkademik tahunAkademik, Hari hari, Ruangan ruangan, LocalTime jamMulai, LocalTime jamSelesai, LocalDate tanggalUjian, Integer limit);
    List<Seminar> findByNote(Note note);

}
