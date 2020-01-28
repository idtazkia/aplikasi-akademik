package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.KelasDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Kelas;
import id.ac.tazkia.akademik.aplikasiakademik.entity.KelasMahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KelasMahasiswaDao extends PagingAndSortingRepository <KelasMahasiswa, String> {
    KelasMahasiswa findByMahasiswaAndKelas(Mahasiswa mahasiswa, Kelas kelas);
    Iterable<KelasMahasiswa> findByKelasAndStatus(Kelas kelas, StatusRecord statusRecord    );
    KelasMahasiswa findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);
    KelasMahasiswa findFirstByKelasAndStatusAndMahasiswaKurikulumNotNull(Kelas kelas, StatusRecord statusRecord);
    Page<KelasMahasiswa> findDistinctByKelasNamaKelasContainingIgnoreCase(String s, Pageable page);
    Page<KelasMahasiswa> findDistinctByKelas(Pageable page);

    @Query("select distinct new id.ac.tazkia.akademik.aplikasiakademik.dto.KelasDto(k.kelas.id,k.kelas.namaKelas,k.mahasiswa.kurikulum.id) from KelasMahasiswa k where k.status= :status")
    List<KelasDto> cariKelas(@Param("status") StatusRecord statusRecord);

    @Query("select distinct km.kelas from KelasMahasiswa km")
    List<Kelas> carikelasMahasiswa ();


}
