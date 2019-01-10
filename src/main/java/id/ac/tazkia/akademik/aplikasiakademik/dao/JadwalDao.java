package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.Prodi;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TahunAkademikProdi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal,String>
{

    List<Jadwal>findByStatusNotIn(StatusRecord statusRecord);
    List<Jadwal>findByTahunAkademikProdi(TahunAkademikProdi tahunAkademikProdi);
    Page<Jadwal> findByStatusNotInAndProdiAndTahunAkademikProdi(StatusRecord statusRecord, Prodi prodi, TahunAkademikProdi tahunAkademikProdi, Pageable page);
}
