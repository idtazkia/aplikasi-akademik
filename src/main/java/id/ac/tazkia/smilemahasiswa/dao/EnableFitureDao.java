package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.EnableFiture;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EnableFitureDao extends PagingAndSortingRepository<EnableFiture,String> {
    EnableFiture findByMahasiswaAndFiturAndEnableAndTahunAkademik(Mahasiswa mhsw, StatusRecord uts, Boolean enabled, TahunAkademik byStatus);
    EnableFiture findByMahasiswaAndFiturAndEnable(Mahasiswa mhsw, StatusRecord uts, Boolean enabled);
    EnableFiture findByMahasiswaAndFiturAndTahunAkademik(Mahasiswa mahasiswa, StatusRecord uts, TahunAkademik tahunAkademik);

    Page<EnableFiture> findByTahunAkademikAndFiturInAndWaktuInputIsNotNullOrTahunAkademikAndFiturInAndWaktuEditIsNotNull(TahunAkademik tahunAkademik, List<StatusRecord> fitur, TahunAkademik tahun, List<StatusRecord> f, Pageable page);

}
