package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.SertifikatTlc;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface SertifikatTlcDao extends PagingAndSortingRepository<SertifikatTlc, String> {

    SertifikatTlc findByMahasiswaAndStatus(Mahasiswa mhs, StatusRecord status);

    List<SertifikatTlc> findByStatusAndMahasiswa(StatusRecord status, Mahasiswa mahasiswa);

    List<SertifikatTlc> findByStatus(StatusRecord status);

}
