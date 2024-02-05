package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.Transkript;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TranskriptDao extends PagingAndSortingRepository<Transkript, String> {

    Transkript findByMahasiswaAndMatakuliahAndSemester(Mahasiswa mahasiswa, Matakuliah matakuliah, String semester);

}
