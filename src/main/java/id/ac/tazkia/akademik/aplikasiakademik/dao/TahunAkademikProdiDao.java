package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademikProdi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TahunAkademikProdiDao extends PagingAndSortingRepository<TahunAkademikProdi, String> {
    List<TahunAkademikProdi> findByStatusNotInOrderByTahunAkademikDesc(StatusRecord statusRecord);
    List<TahunAkademikProdi> findByStatusNotIn(StatusRecord statusRecord);
    List<TahunAkademikProdi> findByStatus(StatusRecord statusRecord);
    List<TahunAkademikProdi> findByTahunAkademik(TahunAkademik tahunAkademik);

    TahunAkademikProdi findByTahunAkademikStatusAndProdi(StatusRecord statusRecord, Prodi prodi);
    TahunAkademikProdi findByTahunAkademikStatus(StatusRecord statusRecord);
    TahunAkademikProdi findByTahunAkademikAndProdi(TahunAkademik tahunAkademik, Prodi prodi);
}
