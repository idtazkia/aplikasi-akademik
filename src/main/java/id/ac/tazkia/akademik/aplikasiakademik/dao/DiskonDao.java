package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Diskon;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TagihanMahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface DiskonDao extends PagingAndSortingRepository<Diskon, String> {
    List<Diskon> findByTagihanMahasiswa(TagihanMahasiswa tagihanMahasiswa);
    List<Diskon> findByTagihanMahasiswaTahunAkademikAndTagihanMahasiswaMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa);
}
