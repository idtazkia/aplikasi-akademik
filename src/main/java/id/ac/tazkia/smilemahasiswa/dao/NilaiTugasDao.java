package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.dto.assesment.BobotDto;
import id.ac.tazkia.smilemahasiswa.dto.report.TugasDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
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

}
