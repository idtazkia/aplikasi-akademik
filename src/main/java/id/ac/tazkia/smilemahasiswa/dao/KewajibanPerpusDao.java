package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.KewajibanPerpus;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KewajibanPerpusDao extends PagingAndSortingRepository<KewajibanPerpus, String> {

    List<KewajibanPerpus> findByStatusOrderById(StatusRecord status);

    Long countByStatus(StatusRecord status);

}
