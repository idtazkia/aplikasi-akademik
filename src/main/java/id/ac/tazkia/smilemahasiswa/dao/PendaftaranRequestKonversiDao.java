package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.konversi.GetRequestKonversi;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranRequestKonversi;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PendaftaranRequestKonversiDao extends PagingAndSortingRepository<PendaftaranRequestKonversi, String> {

    List<PendaftaranRequestKonversi> findByMahasiswa(Mahasiswa mahasiswa);

    @Query(value = "select pr.id,m.nim,m.nama,kk.nama as jenis,p.nama_prodi as prodi, pr.status as status from pendaftaran_request_konversi as pr inner join file_pendaftaran_konversi as fp on pr.id = fp.id_pendaftaran_konversi inner join konfigurasi_konversi as kk on pr.id_konfigurasi_konversi = kk.id inner join mahasiswa as m on pr.id_mahasiswa = m.id inner join prodi as p on m.id_prodi = p.id where (m.nim like %?1% or m.nama like %?1%) and p.id in (?2) and pr.status in (?3) limit ?4 offset ?5",
            countQuery = "select count(*) from pendaftaran_request_konversi as pr inner join file_pendaftaran_konversi as fp on pr.id = fp.id_pendaftaran_konversi inner join konfigurasi_konversi as kk on pr.id_konfigurasi_konversi = kk.id inner join mahasiswa as m on pr.id_mahasiswa = m.id inner join prodi as p on m.id_prodi = p.id where (m.nim like %?1% or m.nama like %?1%) and p.id in (?2) and pr.status = ?3 limit ?4 offset ?5",
            nativeQuery = true)
    List<GetRequestKonversi> listRequestKonversi(String search, List<String> prodi, List<String> status, Integer limit, Integer offset);

    @Query(value = "select coalesce(0,sum((sks_diambil))) as sks from pendaftaran_request_konversi where id_mahasiswa = ?1 and status not in ('REJECTED', 'HAPUS')", nativeQuery = true)
    Integer totalSksKonversi(Mahasiswa mahasiswa);
}
