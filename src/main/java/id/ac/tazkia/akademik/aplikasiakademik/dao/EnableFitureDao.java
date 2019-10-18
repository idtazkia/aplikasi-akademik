package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.EnableFiture;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnableFitureDao extends PagingAndSortingRepository<EnableFiture, String> {

    EnableFiture findByMahasiswaAndFiturAndEnableAndTahunAkademik(Mahasiswa mahasiswa, StatusRecord statusRecord, String fitur, TahunAkademik tahunAkademik);

}
