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

    List<EdomQuestion> findByStatusOrderByNomor(StatusRecord statusRecord);



}
