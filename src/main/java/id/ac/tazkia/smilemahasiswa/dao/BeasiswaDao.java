package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Agama;
import id.ac.tazkia.smilemahasiswa.entity.Beasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface BeasiswaDao extends PagingAndSortingRepository<Beasiswa, String> {
    List<Beasiswa> findByStatusOrderByNamaBeasiswa(StatusRecord aktif);

    List<Beasiswa> findByStatusAndIdNotIn(StatusRecord statusRecord, List<String> id);

    @Query(value = "select a.id, c.nim, c.nama, d.nama_prodi, a.status from mahasiswa_beasiswa as a inner join beasiswa as b on a.id_beasiswa = b.id inner join mahasiswa as c on a.id_mahasiswa = c.id inner join prodi as d on c.id_prodi=d.id where b.id = ?1 and a.status = 'AKTIF' group by a.id_mahasiswa", nativeQuery = true)
    List<Object[]> listBeasiswaMahasiwa(String beasiswa);
}
