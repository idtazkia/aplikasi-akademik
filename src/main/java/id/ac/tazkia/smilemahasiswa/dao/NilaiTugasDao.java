package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.report.TugasDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NilaiTugasDao extends PagingAndSortingRepository<NilaiTugas, String> {
    @Query("select new id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto(nt.krsDetail.id,nt.bobotTugas.id,nt.nilai) from NilaiTugas nt where nt.status = 'AKTIF' and nt.kategoriTugas = :kategori and nt.krsDetail.jadwal = :jadwal and nt.bobotTugas.status = 'AKTIF' order by nt.bobotTugas.pertemuan asc")
    List<BobotDto> nilaiTugasList(@Param("jadwal")Jadwal jadwal, @Param("kategori")KategoriTugas kategoriTugas);

    NilaiTugas findByStatusAndBobotTugasAndKrsDetail(StatusRecord aktif, BobotTugas bobotTugas, KrsDetail krsDetail);

    List<NilaiTugas> findByStatusAndBobotTugas(StatusRecord status, BobotTugas bobotTugas);

//    List<NilaiTugas> findByStatusAndBobot

    List<NilaiTugas> findByStatusAndKrsDetailAndBobotTugasStatus(StatusRecord aktif, KrsDetail krsDetail, StatusRecord aktif1);

    List<NilaiTugas> findByStatusAndKrsDetailAndBobotTugasStatusAndKategoriTugas(StatusRecord aktif, KrsDetail krsDetail, StatusRecord aktif1, KategoriTugas kategoriTugas);

    @Query("select new id.ac.tazkia.smilemahasiswa.dto.report.TugasDto(nt.krsDetail.id,concat(nt.bobotTugas.pertemuan, ' - ', nt.bobotTugas.namaTugas),nt.nilai) from NilaiTugas nt where nt.status = 'AKTIF' and nt.bobotTugas.status = 'AKTIF' and nt.krsDetail.id = :id and nt.kategoriTugas = :kategori order by nt.bobotTugas.pertemuan asc ")
    List<TugasDto> findScore(@Param("id")String id, @Param("kategori")KategoriTugas kategoriTugas);

    List<NilaiTugas> findByBobotTugasIn(List<BobotTugas> listTugas);

    List<NilaiTugas> findByBobotTugasInAndStatus(List<BobotTugas> listTugas, StatusRecord statusRecord);

    @Modifying
    @Query(value = "update krs_nilai_tugas as a,\n" +
            "(select d.id as id_krs_nilai_tugas,round((c.bobot*d.nilai)/100,2) as nilai_tugas from krs_detail as a \n" +
            "inner join jadwal as b on a.id_jadwal = b.id and b.id = ?1 and a.status = 'AKTIF'\n" +
            "inner join jadwal_bobot_tugas as c on c.id_jadwal = b.id and c.status = 'AKTIF'\n" +
            "inner join krs_nilai_tugas as d on c.id = d.id_bobot_tugas and d.id_krs_detail = a.id and d.status = 'AKTIF') as b\n" +
            "set a.nilai_akhir = b.nilai_tugas where a.id = b.id_krs_nilai_tugas", nativeQuery = true)
    void updateNilaiTerbobot(Jadwal jadwal);

    @Modifying
    @Query(value = "update krs_detail as a,\n" +
            "(select id,nilai_presensi,nilai_tugas,nilai_uts, nilai_uas, nilai_akhir, \n" +
            "if(nilai_akhir >= 85,'A',if(nilai_akhir >= 80 , 'A-',if(nilai_akhir >= 75, 'B+',if(nilai_akhir >= 70,'B',if(nilai_akhir >= 65, 'B-',if(nilai_akhir >= 60,'C+',\n" +
            "if(nilai_akhir >= 55,'C',if(nilai_akhir >= 50 , 'D','E')))))))) as grade,\n" +
            "if(nilai_akhir >= 85,4.00,if(nilai_akhir >= 80 , 3.70,if(nilai_akhir >= 75, 3.30,if(nilai_akhir >= 70,3.00,if(nilai_akhir >= 65, 2.70,if(nilai_akhir >= 60,2.30,\n" +
            "if(nilai_akhir >= 55,2.00,if(nilai_akhir >= 50 , 1.00,0.00)))))))) as bobot from\n" +
            "(select id,round(coalesce(nilai_presensi,0),2) as nilai_presensi,round(coalesce(nilai_tugas,0),2) as nilai_tugas, round(coalesce(nilai_uts,0),2) as nilai_uts, round(nilai_uas,2) as nilai_uas\n" +
            ", round(coalesce(nilai_tugas,0) + coalesce(nilai_uts,0) + coalesce(nilai_uas,0) + coalesce(nilai_presensi,0),2) as nilai_akhir from\n" +
            "(select a.id,sum(if(c.kategori_tugas = 'TUGAS',(c.bobot*d.nilai)/100,0)) as nilai_tugas\n" +
            ",sum(if(c.kategori_tugas = 'UTS',(c.bobot*d.nilai)/100,0)) as nilai_UTS\n" +
            ",sum(if(c.kategori_tugas = 'UAS',(c.bobot*d.nilai)/100,0)) as nilai_UAS\n" +
            ",(x.nilai_presensi*b.bobot_presensi)/100 as nilai_presensi from krs_detail as a \n" +
            "inner join jadwal as b on a.id_jadwal = b.id and b.id = :jadwal and a.status = 'AKTIF'\n" +
            "inner join jadwal_bobot_tugas as c on c.id_jadwal = b.id and c.status = 'AKTIF'\n" +
            "inner join krs_nilai_tugas as d on c.id = d.id_bobot_tugas and d.id_krs_detail = a.id and d.status = 'AKTIF'\n" +
            "left join\n" +
            "(select b.id as id_krs_detail,presdos,presensi, (presensi/presdos)*100 as nilai_presensi from\n" +
            "(select id_jadwal,count(a.id) as presdos from presensi_dosen as a where id_jadwal = :jadwal \n" +
            "and status_presensi <> 'MANGKIR' and status = 'AKTIF') as a\n" +
            "left join\n" +
            "(select count(a.id) as presensi,a.id,a.id_jadwal\n" +
            "from krs_detail as a \n" +
            "inner join presensi_mahasiswa as b on b.id_krs_detail = a.id and a.id_jadwal = :jadwal and \n" +
            "a.status = 'AKTIF' and b.status = 'AKTIF' and b.status_presensi not in ('MANGKIR') group by a.id) as b on a.id_jadwal = b.id_jadwal)as x\n" +
            "on a.id = x.id_krs_detail\n" +
            "group by a.id) as a) as a) as b\n" +
            "set a.nilai_presensi = b.nilai_presensi, a.nilai_tugas = b.nilai_tugas, a.nilai_uts = b.nilai_uts, a.nilai_uas = b.nilai_uas, a.nilai_akhir = b.nilai_akhir,\n" +
            "a.grade = b.grade, a.bobot = b.bobot\n" +
            "where a.id = b.id and a.id_jadwal = :jadwal", nativeQuery = true)
    void updateNilaiTotalKrsDetail(@Param("jadwal") Jadwal jadwal);

}
