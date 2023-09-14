package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PendaftaranMbkmDao extends PagingAndSortingRepository<PendaftaranMbkm, String> {

    PendaftaranMbkm findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik tahun, StatusRecord status);

    List<PendaftaranMbkm> findByDosenAndStatus(Dosen dosen, StatusRecord statusRecord);

}
