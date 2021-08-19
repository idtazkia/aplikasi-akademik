package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Kelas;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KelasDao extends PagingAndSortingRepository<Kelas,String> {
    Page<Kelas> findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(List<StatusRecord> asList, String search, Pageable page);

    Page<Kelas> findByStatusNotIn(List<StatusRecord> asList, Pageable page);

    @Query(value = "select a.id, a.nama_kelas from kelas as a inner join prodi as b on a.id_prodi = b.id where b.id = ?1 and a.angkatan = ?2", nativeQuery = true)
    List<Object[]> kelasAngktanProdi(String idProdi, String angkatan);

    List<Kelas> findByStatusAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord statusRecord, String sp);

    @Query(value = "select k.id,k.nama_kelas from kelas_mahasiswa as km inner join kelas as k on km.id_kelas = k.id inner join prodi as p on k.id_prodi = p.id inner join jenjang as j on p.id_jenjang = j.id inner join mahasiswa as m on km.id_mahasiswa = m.id where km.status = 'AKTIF'and m.status= 'AKTIF' and k.status ='AKTIF' and k.id_kurikulum is not null group by k.id order by k.nama_kelas asc",nativeQuery = true)
    List<Object[]> kelasPloting();
}
