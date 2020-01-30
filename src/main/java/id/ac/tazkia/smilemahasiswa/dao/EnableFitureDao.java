package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.EnableFiture;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnableFitureDao extends PagingAndSortingRepository<EnableFiture,String> {
    EnableFiture findByMahasiswaAndFiturAndEnableAndTahunAkademik(Mahasiswa mhsw, StatusRecord uts, String s, TahunAkademik byStatus);
}
