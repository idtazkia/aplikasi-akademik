package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranMbkm;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PendaftaranMbkmDao extends PagingAndSortingRepository<PendaftaranMbkm, String> {

    PendaftaranMbkm findByMahasiswaAndTahunAkademikAndStatus(Mahasiswa mahasiswa, TahunAkademik tahun, StatusRecord status);

}
