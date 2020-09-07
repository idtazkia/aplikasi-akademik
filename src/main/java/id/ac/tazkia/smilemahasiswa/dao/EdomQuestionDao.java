package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.EdomQuestion;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface EdomQuestionDao extends PagingAndSortingRepository<EdomQuestion, String> {

    List<EdomQuestion> findByStatusAndBahasaOrderByNomor(StatusRecord statusRecord, String Bahasa);

    Page<EdomQuestion> findByStatusOrderByBahasa(StatusRecord statusRecord, Pageable page);


    Page<EdomQuestion> findByStatusAndTahunAkademikAndBahasaAndPertanyaanContainingIgnoreCaseOrderByNomor(StatusRecord statusRecord, TahunAkademik tahunAkademik, String bahasa, String search, Pageable page);

    Page<EdomQuestion> findByStatusAndTahunAkademikAndBahasaOrderByNomor(StatusRecord statusRecord, TahunAkademik tahunAkademik, String bahasa, Pageable page);

    Page<EdomQuestion> findByStatusAndTahunAkademikAndPertanyaanContainingIgnoreCaseOrderByNomor(StatusRecord statusRecord, TahunAkademik tahunAkademik, String search, Pageable page);

    Page<EdomQuestion> findByStatusAndTahunAkademikOrderByNomor(StatusRecord statusRecord, TahunAkademik tahunAkademik, Pageable page);

    Page<EdomQuestion> findByStatusAndBahasaAndPertanyaanContainingIgnoreCaseOrderByNomor(StatusRecord statusRecord, String bahasa, String search, Pageable page);

    Page<EdomQuestion> findByStatusAndBahasaOrderByNomor(StatusRecord statusRecord, String Bahasa, Pageable page);

    Page<EdomQuestion> findByStatusAndPertanyaanContainingIgnoreCaseOrderByNomor(StatusRecord statusRecord, String search, Pageable page);



}
