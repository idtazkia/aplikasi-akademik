package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademik;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademikProdi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface TahunAkademikProdiDao extends PagingAndSortingRepository<TahunAkademikProdi, String> {
    TahunAkademikProdi findByStatusNotIn(StatusRecord statusRecord);

    List<TahunAkademikProdi>findByStatusNotInAndTahunAkademikAndProdi(StatusRecord statusRecord, TahunAkademik tahunAkademik, Prodi prodi);
}
