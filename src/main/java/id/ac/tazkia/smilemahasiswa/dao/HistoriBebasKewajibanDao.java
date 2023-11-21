package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.ByBagian;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.HistoriBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface HistoriBebasKewajibanDao extends PagingAndSortingRepository<HistoriBebasKewajiban, String> {

    HistoriBebasKewajiban findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(PendaftaranBebasKewajiban bebasKewajiban, ByBagian bagian, StatusRecord status);

    List<HistoriBebasKewajiban> findByBebasKewajibanAndByBagianAndStatusOrderByWaktuInput(PendaftaranBebasKewajiban pendaftaranBebasKewajiban, ByBagian bagian, StatusRecord status);

    List<HistoriBebasKewajiban> findByByBagianAndStatusOrderByWaktuInput(ByBagian bagian, StatusRecord status);

}
