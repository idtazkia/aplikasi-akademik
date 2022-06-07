package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.EnableFiture;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface EnableFitureDao extends PagingAndSortingRepository<EnableFiture,String> {
    EnableFiture findByMahasiswaAndFiturAndEnableAndTahunAkademik(Mahasiswa mhsw, StatusRecord uts, Boolean enabled, TahunAkademik byStatus);
    EnableFiture findByMahasiswaAndFiturAndEnable(Mahasiswa mhsw, StatusRecord uts, Boolean enabled);
    EnableFiture findByMahasiswaAndFiturAndTahunAkademik(Mahasiswa mahasiswa, StatusRecord uts, TahunAkademik tahunAkademik);

}
