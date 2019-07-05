package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Mahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TagihanMahasiswa;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TagihanMahasiwaDao extends PagingAndSortingRepository<TagihanMahasiswa,String> {
    List<TagihanMahasiswa> findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
    List<TagihanMahasiswa> findByMahasiswaAndTahunAkademikAndLunas(Mahasiswa mahasiswa, TahunAkademik tahunAkademik,String lunas);

}
