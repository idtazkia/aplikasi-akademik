package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.ByBagian;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.HistoriBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoriBebasKewajibanDao extends PagingAndSortingRepository<HistoriBebasKewajiban, String> {

    HistoriBebasKewajiban findFirstByBebasKewajibanAndByBagianAndStatusOrderByWaktuInputDesc(PendaftaranBebasKewajiban bebasKewajiban, ByBagian bagian, StatusRecord status);

    List<HistoriBebasKewajiban> findByBebasKewajibanAndByBagianAndStatusOrderByWaktuInput(PendaftaranBebasKewajiban pendaftaranBebasKewajiban, ByBagian bagian, StatusRecord status);

    List<HistoriBebasKewajiban> findByByBagianAndStatusOrderByWaktuInput(ByBagian bagian, StatusRecord status);

    @Query("SELECT hb FROM HistoriBebasKewajiban hb where hb.bebasKewajiban.id=:idPendaftaran group by hb.byBagian order by hb.urutan")
    List<HistoriBebasKewajiban> getDetailAkademik(@Param("idPendaftaran") String id);

}
