package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.time.LocalDate;
import java.util.List;

public interface EnableKrsDao extends PagingAndSortingRepository<EnableKrs, String> {

    Page<EnableKrs> findByTahunAkademik(TahunAkademik tahuu, Pageable page);

    EnableKrs findByTahunAkademikAndMahasiswaAndStatus(TahunAkademik tahunAkademik, Mahasiswa mahasiswa, StatusRecord status);

    List<EnableKrs> findByTanggalKadaluarsaAndStatus(LocalDate now, StatusRecord status);

}
