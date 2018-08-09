package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.RunningNumber;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RunningNumberDao extends PagingAndSortingRepository<RunningNumber,String> {
    RunningNumber findByNama(String nama);
}
