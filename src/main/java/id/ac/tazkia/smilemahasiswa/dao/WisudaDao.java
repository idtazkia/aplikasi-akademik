package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface WisudaDao extends PagingAndSortingRepository<Wisuda, String> {
    Page<Wisuda> findByPeriodeWisudaAndStatusNotInOrderByStatusDescMahasiswaIdProdiAsc(PeriodeWisuda periodeWisuda, List<StatusApprove> statusApproves, Pageable page);
    Page<Wisuda> findByPeriodeWisudaOrderByStatusDescMahasiswaIdProdiAsc(PeriodeWisuda periodeWisuda, Pageable page);
    Page<Wisuda> findByPeriodeWisudaAndMahasiswaIdProdiAndStatusNotInOrderByStatusDesc(PeriodeWisuda periodeWisuda, Prodi prodi, List<StatusApprove> statusApproves,Pageable page);
    List<Wisuda> findByMahasiswaAndStatusNotIn(Mahasiswa mahasiswa, List<StatusApprove> statusApprove);
    Wisuda findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusApprove statusApprove);
    List<Wisuda> findByMahasiswa(Mahasiswa mahasiswa);
    Wisuda findFirstByMahasiswaAndStatus(Mahasiswa mahasiswa,StatusApprove statusApprove);
    List<Wisuda> findByStatusAndPeriodeWisudaOrderByMahasiswaIdProdiIdAscMahasiswaNimAsc(StatusApprove status, PeriodeWisuda periodeWisuda);
    List<Wisuda> findByStatusAndPeriodeWisudaAndMahasiswaIdProdiOrderByMahasiswaNimAsc(StatusApprove status, PeriodeWisuda periodeWisuda,Prodi prodi);
    List<Wisuda> findByStatusAndPeriodeWisudaAndMahasiswaIdProdiIdJenjangOrderByMahasiswaIdProdiIdAscMahasiswaNimAsc(StatusApprove status, PeriodeWisuda periodeWisuda,Jenjang jenjang);

    @Query(value = "select w.id_mahasiswa from wisuda as w inner join mahasiswa as m on w.id_mahasiswa = m.id where w.id_periode = ?1 and w.status = 'APPROVED' and m.id_prodi = ?2", nativeQuery = true)
    List<String> cariIdMahasiswa(PeriodeWisuda periodeWisuda,String prodi);

    @Query(value = "select w.id_mahasiswa from wisuda as w inner join mahasiswa as m on w.id_mahasiswa = m.id inner join prodi as p on m.id_prodi = p.id inner join jenjang as j on p.id_jenjang = j.id where w.id_periode = ?1 and w.status = 'APPROVED' and j.id = ?2", nativeQuery = true)
    List<String> cariIdMahasiswaJenjang(PeriodeWisuda periodeWisuda,String jenjang);

    @Query(value = "select distinct m.nim,m.nama,p.nama_prodi,f.nama_fakultas,m.jenis_kelamin,p.id,coalesce(m.tanggal_lulus,'-')as tanggal,m.nik,m.tempat_lahir,m.tanggal_lahir,m.ukuran_baju," +
            "m.judul,m.title, gg.ipk2 as ipk,coalesce(b.nama_beasiswa,'-')as beasiswa,m.id as idMahasiswa, if(ipk2 >= 3.80,'Pujian Tertinggi',if(ipk2 >= 3.50,'Pujian',if(ipk2 >= 3.00,'Sangat Memuaskan','Memuaskan'))) as predikat\n" +
            " from sidang as s inner join seminar as se on s.id_seminar = se.id \n" +
            "inner join note as n on se.id_note = n.id\n" +
            "inner join mahasiswa as m on n.id_mahasiswa = m.id\n" +
            "inner join prodi as p on m.id_prodi = p.id\n" +
            "inner join fakultas as f on p.id_fakultas = f.id\n" +
            "left join beasiswa as b on m.id_beasiswa = b.id\n" +
            "left join wisuda as w on w.id_mahasiswa = m.id\n" +
            "inner join (\n" +
            "select b.id as idMahasiswa,b.nim, b.nama, sum(mutu)/sum(sks) as ipk1, round(sum(mutu)/sum(sks),2) as ipk2 from\n" +
            "(select id_mahasiswa,semester,id_matakuliah_kurikulum,kode_matakuliah as kode, nama_matakuliah as matkul, nama_matakuliah_english as course , jumlah_sks as sks, max(nilai_akhir) as nilai_akhir,b.bobot,b.nama as grade,b.bobot*jumlah_sks as mutu from \n" +
            "(select id_mahasiswa,semester,id_matakuliah_kurikulum,kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,max(nilai_akhir) as nilai_akhir, max(bobot)as bobot, min(grade)as grade from \n" +
            "\t(select a.id_mahasiswa,d.semester,c.id_matakuliah_kurikulum,e.kode_matakuliah, e.nama_matakuliah, e.nama_matakuliah_english, d.jumlah_sks, max(a.nilai_akhir) as nilai_akhir, max(a.bobot)as bobot, min(a.grade)as grade from krs_detail as a inner join krs as b on a.id_krs = b.id inner join jadwal as c on a.id_jadwal = c.id inner join matakuliah_kurikulum as d on c.id_matakuliah_kurikulum = d.id inner join matakuliah as e on d.id_matakuliah = e.id where a.status = 'AKTIF' and  a.finalisasi = 'FINAL' and a.nilai_akhir is not null and a.nilai_akhir > 0 and a.id_mahasiswa in (?1) and d.jumlah_sks > 0 group by e.kode_matakuliah, id_mahasiswa)a group by nama_matakuliah, id_mahasiswa)a inner join grade as b on coalesce(a.nilai_akhir,0) <= b.atas and coalesce(a.nilai_akhir,0) >= b.bawah group by nama_matakuliah_english, id_mahasiswa) a inner join mahasiswa as b on a.id_mahasiswa = b.id group by id_mahasiswa)gg on m.id = gg.idMahasiswa\n" +
            "where s.akademik in ('WAITING','APPROVED') and s.status_sidang not in ('REJECTED') and p.id = ?2  order by p.id asc;", nativeQuery = true)
    List<Object[]> getDetailWisuda(List<String> mahasiswa,String id);

    @Query(value = "select distinct m.nim,m.nama,p.nama_prodi,f.nama_fakultas,m.jenis_kelamin,p.id,coalesce(m.tanggal_lulus,'-')as tanggal,m.nik,m.tempat_lahir,m.tanggal_lahir,m.ukuran_baju," +
            "m.judul,m.title, gg.ipk2 as ipk,coalesce(b.nama_beasiswa,'-')as beasiswa,m.id as idMahasiswa, if(ipk2 >= 3.80,'Pujian Tertinggi',if(ipk2 >= 3.50,'Pujian',if(ipk2 >= 3.00,'Sangat Memuaskan','Memuaskan'))) as predikat\n" +
            " from sidang as s inner join seminar as se on s.id_seminar = se.id \n" +
            "inner join note as n on se.id_note = n.id\n" +
            "inner join mahasiswa as m on n.id_mahasiswa = m.id\n" +
            "inner join prodi as p on m.id_prodi = p.id\n" +
            "inner join jenjang as j on p.id_jenjang = j.id\n" +
            "inner join fakultas as f on p.id_fakultas = f.id\n" +
            "left join beasiswa as b on m.id_beasiswa = b.id\n" +
            "left join wisuda as w on w.id_mahasiswa = m.id\n" +
            "inner join (\n" +
            "select b.id as idMahasiswa,b.nim, b.nama, sum(mutu)/sum(sks) as ipk1, round(sum(mutu)/sum(sks),2) as ipk2 from\n" +
            "(select id_mahasiswa,semester,id_matakuliah_kurikulum,kode_matakuliah as kode, nama_matakuliah as matkul, nama_matakuliah_english as course , jumlah_sks as sks, max(nilai_akhir) as nilai_akhir,b.bobot,b.nama as grade,b.bobot*jumlah_sks as mutu from \n" +
            "(select id_mahasiswa,semester,id_matakuliah_kurikulum,kode_matakuliah, nama_matakuliah, nama_matakuliah_english, jumlah_sks,max(nilai_akhir) as nilai_akhir, max(bobot)as bobot, min(grade)as grade from \n" +
            "\t(select a.id_mahasiswa,d.semester,c.id_matakuliah_kurikulum,e.kode_matakuliah, e.nama_matakuliah, e.nama_matakuliah_english, d.jumlah_sks, max(a.nilai_akhir) as nilai_akhir, max(a.bobot)as bobot, min(a.grade)as grade from krs_detail as a inner join krs as b on a.id_krs = b.id inner join jadwal as c on a.id_jadwal = c.id inner join matakuliah_kurikulum as d on c.id_matakuliah_kurikulum = d.id inner join matakuliah as e on d.id_matakuliah = e.id where a.status = 'AKTIF' and  a.finalisasi = 'FINAL' and a.nilai_akhir is not null and a.nilai_akhir > 0 and a.id_mahasiswa in (?1) and d.jumlah_sks > 0 group by e.kode_matakuliah, id_mahasiswa)a group by nama_matakuliah, id_mahasiswa)a inner join grade as b on coalesce(a.nilai_akhir,0) <= b.atas and coalesce(a.nilai_akhir,0) >= b.bawah group by nama_matakuliah_english, id_mahasiswa) a inner join mahasiswa as b on a.id_mahasiswa = b.id group by id_mahasiswa)gg on m.id = gg.idMahasiswa\n" +
            "where s.akademik in ('WAITING','APPROVED') and s.status_sidang not in ('REJECTED') and j.id = ?2  order by p.id asc;", nativeQuery = true)
    List<Object[]> getDetailWisudaJenjang(List<String> mahasiswa,String id);
}
