package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.MahasiswaDosenWaliDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MahasiswaDosenWaliDao extends PagingAndSortingRepository <MahasiswaDosenWali, String> {
    MahasiswaDosenWali findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

//    Page<MahasiswaDosenWali> findByStatusAndDosenOrderByMahasiswaNimDesc(StatusRecord statusRecord, Dosen dosen, Pageable page);


    @Query(value = "SELECT a.id AS idMahasiswa,b.id AS idMahasiswaDosenWali,a.nim AS nim, a.nama AS namaMahasiswa, g.nama_prodi as namaProdi, coalesce(d.nama_karyawan,'Belum di set') AS dosenWali FROM mahasiswa AS a\n" +
            "LEFT JOIN prodi AS g ON a.id_prodi = g.id\n" +
            "LEFT JOIN (SELECT * FROM mahasiswa_dosen_wali WHERE STATUS = 'AKTIF') AS b ON a.id = b.id_mahasiswa\n" +
            "LEFT JOIN dosen AS c ON b.id_dosen = c.id\n" +
            "LEFT JOIN karyawan AS d ON c.id_karyawan = d.id \n" +
            "WHERE a.status = 'AKTIF' \n" +
            "ORDER BY a.angkatan DESC, a.nim\n" , nativeQuery = true ,countQuery = "select count(id) as jml from mahasiswa where status = 'AKTIF'")
    Page<MahasiswaDosenWaliDto> listMahasiswaDosenWali(Pageable page);

    @Query(value = "SELECT a.id AS idMahasiswa,b.id AS idMahasiswaDosenWali,a.nim AS nim, a.nama AS namaMahasiswa, g.nama_prodi as namaProdi, coalesce(d.nama_karyawan,'Belum di set') AS dosenWali FROM mahasiswa AS a\n" +
            "LEFT JOIN prodi AS g ON a.id_prodi = g.id\n" +
            "LEFT JOIN (SELECT * FROM mahasiswa_dosen_wali WHERE STATUS = 'AKTIF') AS b ON a.id = b.id_mahasiswa\n" +
            "LEFT JOIN dosen AS c ON b.id_dosen = c.id\n" +
            "LEFT JOIN karyawan AS d ON c.id_karyawan = d.id \n" +
            "WHERE a.status = 'AKTIF' AND a.id_prodi = ?1 \n" +
            "ORDER BY a.angkatan DESC, a.nim\n" , nativeQuery = true ,countQuery = "select count(id) as jml from mahasiswa where status = 'AKTIF' and id_prodi = ?1")
    Page<MahasiswaDosenWaliDto> listMahasiswaDosenWaliProdi(String idProdi, Pageable page);

    @Query(value = "select m.id,m.nim,m.nama,k.nama_karyawan from mahasiswa as m inner join dosen as d on m.id_dosen_wali = d.id inner join karyawan as k on d.id_karyawan = k.id where m.status = 'AKTIF' and m.angkatan = ?1 and m.id_prodi = ?2 order by m.nim asc " , nativeQuery = true)
    List<Object[]> listMahasiswa(String angkatan, Prodi prodi);

    @Query(value = "select m.id,m.nim,m.nama,k.nama_karyawan \n" +
            "from mahasiswa as m \n" +
            "inner join dosen as d on m.id_dosen_wali = d.id \n" +
            "inner join karyawan as k on d.id_karyawan = k.id \n" +
            "where m.status = 'AKTIF' and m.angkatan = ?1 and m.id_prodi = ?2 and m.id_dosen_wali = ?3\n" +
            "order by m.nim asc" , nativeQuery = true)
    List<Object[]> listMahasiswaByDosen(String angkatan, Prodi prodi, String dosen);


}
