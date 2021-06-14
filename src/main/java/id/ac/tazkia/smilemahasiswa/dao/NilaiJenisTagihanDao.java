package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.objenesis.instantiator.basic.NewInstanceInstantiator;

import java.util.List;

public interface NilaiJenisTagihanDao extends PagingAndSortingRepository<NilaiJenisTagihan, String> {

    Page<NilaiJenisTagihan> findByStatusNotInAndJenisTagihanNamaContainingIgnoreCaseOrProdiNamaProdiContainingIgnoreCaseOrderByAngkatanDesc(List<StatusRecord> asList, String angkatan, String prodi, Pageable page);

    Page<NilaiJenisTagihan> findByStatusNotInOrderByAngkatan(List<StatusRecord> asList, Pageable page);

    List<NilaiJenisTagihan> findByStatusOrderByJenisTagihan(StatusRecord statusRecord);

    Integer countByStatusAndJenisTagihan(StatusRecord statusRecord, JenisTagihan jenisTagihan);

    List<NilaiJenisTagihan> findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(TahunAkademik tahunAkademik, String angkatan , Prodi prodi, Program program, StatusRecord statusRecord);

    List<NilaiJenisTagihan> findByTahunAkademikAndProdi(TahunAkademik tahunAkademik, Prodi prodi);

    NilaiJenisTagihan findByJenisTagihanIdAndTahunAkademikAndProdiAndAngkatanAndProgramAndStatus(String jenisTagihan,
                                                                                                 TahunAkademik tahunAkademik,
                                                                                                 Prodi prodi, String angkatan,
                                                                                                 Program program, StatusRecord statusRecord);

    NilaiJenisTagihan findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatus(Prodi prodi, String Angkatan, TahunAkademik tahunAkademik, Program program,
                                                                                StatusRecord statusRecord);

}
