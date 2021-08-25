package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.DaftarUlang;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface DaftarUlangDao extends PagingAndSortingRepository<DaftarUlang, String> {
    DaftarUlang findByStatusAndMahasiswaAndTahunAkademik(StatusRecord statusRecord, Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
}
