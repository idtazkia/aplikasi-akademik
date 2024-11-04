package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TugasAkhir;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TugasAkhirDao extends PagingAndSortingRepository<TugasAkhir, String> {
    TugasAkhir findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord status);
}
