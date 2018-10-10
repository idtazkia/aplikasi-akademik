package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Krs;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KrsDao extends PagingAndSortingRepository<Krs,String> {
    Krs findByTahunAkademikStatusAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
    Krs findByTahunAkademikStatus(StatusRecord S);
}
