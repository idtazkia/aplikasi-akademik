package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Krs;
import id.ac.tazkia.akademik.aplikasiakademik.entity.KrsDetail;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
    List<KrsDetail> findByKrsAndAndMahasiswaAndJadwalIdHariId(Krs krs, Mahasiswa mahasiswa,String hari);
   List<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa);

   // List<KrsDetail> findByStatusNotInAndMahasiswa(StatusRecord statusRecord, Mahasiswa mahasiswa);
}
