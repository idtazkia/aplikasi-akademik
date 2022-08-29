package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.MahasiswaCicilan;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MahasiswaCicilanDao extends PagingAndSortingRepository<MahasiswaCicilan, String> {

    MahasiswaCicilan findByStatusAndMahasiswa(StatusRecord status, Mahasiswa mahasiswa);

}
