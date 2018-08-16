package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Program;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TahunAkademikDao extends PagingAndSortingRepository<TahunAkademik, String> {
    Page<TahunAkademik> findByStatus(StatusRecord status, Pageable page);
    Page<TahunAkademik> findByStatusOrStatus(StatusRecord status,StatusRecord statusn, Pageable page);
    Page<TahunAkademik> findByStatusAndProdiAndProgram(StatusRecord status, Prodi prodi, Program program, Pageable page);
    Page<TahunAkademik> findByStatusOrStatusAndProdiAndProgram(StatusRecord aktif, StatusRecord nonaktif, Prodi prodi, Program program, Pageable page);

    Page<TahunAkademik> findByProdiAndProgramAndStatusNotIn(Prodi prodi, Program program, StatusRecord status, Pageable page);
}
