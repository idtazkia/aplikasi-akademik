package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.KewajibanSertifikatTlc;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KewajibanSertifikatTlcDao extends PagingAndSortingRepository<KewajibanSertifikatTlc, String> {

    KewajibanSertifikatTlc findByBebasKewajibanAndStatus(PendaftaranBebasKewajiban bebasKewajiban, StatusRecord status);

    List<KewajibanSertifikatTlc> findByStatusAndBebasKewajiban(StatusRecord status, PendaftaranBebasKewajiban bebasKewajiban);

    List<KewajibanSertifikatTlc> findByStatus(StatusRecord status);

}
