package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademikProdi;
import org.apache.kafka.common.metrics.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TahunProdiDao extends PagingAndSortingRepository<TahunAkademikProdi, String> {

    List<TahunAkademikProdi> findByStatus(StatusRecord aktif);

    List<TahunAkademikProdi> findByTahunAkademik(TahunAkademik akademik);

    TahunAkademikProdi findByStatusAndTahunAkademikAndProdi(StatusRecord status, TahunAkademik tahun, Prodi prodi);

    Iterable<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikDesc(List<StatusRecord >hapus);

    TahunAkademikProdi findByTahunAkademikAndProdi(TahunAkademik tahunAkademik, Prodi prodi);

    Page<TahunAkademikProdi> findByStatusOrderByTahunAkademikKodeTahunAkademikDesc(StatusRecord statusRecord, Pageable page);

}
