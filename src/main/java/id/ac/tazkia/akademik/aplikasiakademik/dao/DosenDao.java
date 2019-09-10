package id.ac.tazkia.akademik.aplikasiakademik.dao;


import id.ac.tazkia.akademik.aplikasiakademik.entity.Dosen;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DosenDao extends PagingAndSortingRepository<Dosen, String> {

    Long countDosenByStatus(StatusRecord statusRecord);
    Iterable<Dosen> findByStatusNotIn(StatusRecord statusRecord);
    Page<Dosen> findByStatusNotIn(StatusRecord statusRecord, Pageable page);
    Page<Dosen> findByStatusNotInAndKaryawanNamaKaryawanContainingIgnoreCaseOrKaryawanNikContainingIgnoreCase(StatusRecord statusRecord, String nama,String nip,Pageable page);
    Dosen findByKaryawan(Karyawan karyawan);
    Dosen findByKaryawanRfid(String string);

}
