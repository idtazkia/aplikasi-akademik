package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.JadwalBahasa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalBahasaDao extends PagingAndSortingRepository<JadwalBahasa, String> {
    List<JadwalBahasa> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord status);

    @Query(value = "select jb.* from jadwal_bahasa as jb inner join jadwal_dosen_bahasa as jdh on jb.id = jdh.id_jadwal_bahasa where jb.status = 'AKTIF' and jdh.id_dosen = ?1 and jb.id_tahun_akademik = ?2", nativeQuery = true)
    List<JadwalBahasa> findByDosen(Dosen dosen, TahunAkademik tahunAkademik);
}
