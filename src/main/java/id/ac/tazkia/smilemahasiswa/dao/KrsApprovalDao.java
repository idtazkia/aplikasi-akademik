package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.krs.MatkulMengulangDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsApprovalDao extends PagingAndSortingRepository<KrsApproval, String> {

    KrsApproval findByKrsAndMahasiswaAndStatusNotIn(Krs krs, Mahasiswa mahasiswa, List<StatusApprove> status);
    KrsApproval findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa);

    Page<KrsApproval> findByMahasiswaDosen(Dosen dosen, Pageable page);
    @Query(value = "SELECT a.id as idKrs, c.nim,c.nama,d.nama_prodi, c.angkatan, coalesce(e.ipk,0) AS ip_semester, coalesce(b.sks_total,0) as sks_total,coalesce(b.ipk ,0) as ipk, a.approval, a.idApproval \n" +
            "FROM (SELECT a.*, ka.id as idApproval, ka.status as approval FROM krs as a inner join mahasiswa as b on a.id_mahasiswa=b.id \n" +
            "inner join tahun_akademik as c on a.id_tahun_akademik = c.id inner join krs_approval as ka on a.id=ka.id_krs\n" +
            "WHERE a.STATUS='AKTIF' AND a.id_tahun_akademik=?1 and b.status_aktif='AKTIF' and ka.status not in('REJECTED', 'HAPUS') and b.id_dosen_wali=?3)a \n" +
            "LEFT JOIN \n" +
            "(SELECT a.id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk \n" +
            "FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b ON a.id_matakuliah_kurikulum=b.id \n" +
            "inner join krs as c on a.id_krs = c.id inner join tahun_akademik as d on c.id_tahun_akademik = d.id\n" +
            "WHERE a.status='AKTIF' AND b.jumlah_sks > 0 AND a.finalisasi = 'FINAL' AND a.id_mahasiswa IS NOT NULL and d.kode_tahun_akademik <= ?4 \n" +
            "GROUP BY a.id_mahasiswa)b \n" +
            "ON a.id_mahasiswa=b.id_mahasiswa \n" +
            "INNER JOIN mahasiswa AS c ON a.id_mahasiswa = c.id \n" +
            "INNER JOIN prodi AS d ON c.id_prodi=d.id \n" +
            "LEFT JOIN (SELECT a.id_mahasiswa,ROUND(SUM(b.jumlah_sks),2) AS sks_total,ROUND(SUM(COALESCE(a.bobot,0)*b.jumlah_sks)/SUM(b.jumlah_sks),2)AS ipk \n" +
            "FROM krs_detail AS a INNER JOIN matakuliah_kurikulum AS b  ON a.id_matakuliah_kurikulum=b.id inner join mahasiswa as c on a.id_mahasiswa=c.id \n" +
            "inner join krs as d on a.id_krs = d.id inner join tahun_akademik as e on d.id_tahun_akademik = e.id\n" +
            "WHERE a.status='AKTIF' AND d.id_tahun_akademik=?2 \n" +
            "AND b.jumlah_sks > 0 AND a.id_mahasiswa IS NOT NULL and c.id_dosen_wali=?3 GROUP BY a.id_mahasiswa)e ON a.id_mahasiswa=e.id_mahasiswa \n" +
            "ORDER BY d.kode_prodi, c.nim", nativeQuery = true)
    List<Object[]> krsApproval(TahunAkademik tahunNow, TahunAkademik tahunBefore, String idDosen, String kode);

    @Query(value = "select k.id as idKrs, kd.id_mahasiswa as idMahasiswa, m.nama_matakuliah as namaMatakuliah, kd.nilai_akhir as nilaiAkhir, kd.bobot, kd.grade from krs k " +
            "inner join krs_detail kd on k.id=kd.id_krs inner join mahasiswa mh on kd.id_mahasiswa=mh.id inner join tahun_akademik ta on k.id_tahun_akademik=ta.id " +
            "inner join matakuliah_kurikulum mk on kd.id_matakuliah_kurikulum=mk.id inner join matakuliah m on mk.id_matakuliah=m.id " +
            "where ta.kode_tahun_akademik <= ?1 and k.status='AKTIF' and kd.status='AKTIF' and kd.finalisasi='FINAL' and kd.id_mahasiswa IS NOT NULL " +
            "and kd.grade >= 'D' and m.kode_matakuliah not like '%sds%' and mh.status_aktif='AKTIF' and k.id_mahasiswa=?2", nativeQuery = true)
    List<MatkulMengulangDto> listMatkulMengulang(String kodeTahunAkademikBefore, String idMahasiswa);

}
