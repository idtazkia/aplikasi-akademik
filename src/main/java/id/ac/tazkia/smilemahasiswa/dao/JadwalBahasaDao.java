package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.JadwalMahasiswa;
import id.ac.tazkia.smilemahasiswa.dto.tlc.jadwal.JadwalRequest;
import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

public interface JadwalBahasaDao extends PagingAndSortingRepository<JadwalBahasa, String> {
    List<JadwalBahasa> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord status);

    @Query(value = "select jb.id as id, jb.nama_jadwal as nama, jb.nama_kelas as kelas, jb.jenis from jadwal_bahasa as jb inner join jadwal_dosen_bahasa as jdh on jb.id = jdh.id_jadwal_bahasa where jb.status = 'AKTIF' and jdh.id_dosen = ?1 and jb.id_tahun_akademik = ?2",
            nativeQuery = true)
    List<JadwalRequest> findByDosen(Dosen dosen, TahunAkademik tahunAkademik);

    @Query(value = "select id,nama_jadwal as nama, nama_kelas as kelas, jenis from jadwal_bahasa where id_tahun_akademik = ?1 and status = 'AKTIF'",
            nativeQuery = true)
    List<JadwalRequest> getJadwalBahasa(TahunAkademik tahunAkademik);

    @Query(value = "select jb.id as jadwal,m.id,m.nim,m.nama from mahasiswa_jadwal_bahasa as mjb inner join jadwal_bahasa as jb on mjb.id_jadwal_bahasa = jb.id inner join mahasiswa as m on mjb.id_mahasiswa = m.id\n"
                    + //
                    "where mjb.id_jadwal_bahasa = ?1", nativeQuery = true)
    List<JadwalMahasiswa> getJadwalMahasiswas(JadwalBahasa jadwalBahasa);

    @Query(value = "select id_mahasiswa from mahasiswa_jadwal_bahasa where id_jadwal_bahasa = ?1 and id_mahasiswa = ?2",
                    nativeQuery = true)
    String cekMahasiswaJadwal(JadwalBahasa jadwalBahasa, Mahasiswa mahasiswa);

    @Modifying
    @Query(value = "delete from mahasiswa_jadwal_bahasa where id_jadwal_bahasa = ?1 and id_mahasiswa = ?2",
                    nativeQuery = true)
    void deleteMahasiswa(JadwalBahasa jadwalBahasa, Mahasiswa mahasiswa);

}
