package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.KewajibanPerpusMahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KewajibanPerpusMahasiswaDao extends PagingAndSortingRepository<KewajibanPerpusMahasiswa, String> {

    List<KewajibanPerpusMahasiswa> findByBebasKewajibanOrderByKewajibanPerpusId(PendaftaranBebasKewajiban pendaftaranBebasKewajiban);

    Long countByBebasKewajibanAndStatus(PendaftaranBebasKewajiban bebasKewajiban, StatusRecord status);

}
