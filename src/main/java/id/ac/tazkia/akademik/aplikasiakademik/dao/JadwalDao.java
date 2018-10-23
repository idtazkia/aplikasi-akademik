package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Jadwal;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface JadwalDao extends PagingAndSortingRepository<Jadwal,String>
{

    List<Jadwal>findByStatusNotIn(StatusRecord statusRecord);

}
