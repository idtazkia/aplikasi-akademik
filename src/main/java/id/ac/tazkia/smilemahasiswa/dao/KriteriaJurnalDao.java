package id.ac.tazkia.smilemahasiswa.dao;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.Jenjang;
import id.ac.tazkia.smilemahasiswa.entity.KriteriaJurnal;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;

import java.util.List;


public interface KriteriaJurnalDao extends PagingAndSortingRepository<KriteriaJurnal, String> {
    List<KriteriaJurnal> findByJenjangAndStatus(Jenjang jenjang,StatusRecord status);    
}
