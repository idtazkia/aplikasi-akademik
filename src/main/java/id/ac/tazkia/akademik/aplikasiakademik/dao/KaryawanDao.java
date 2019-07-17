package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.Karyawan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.StatusRecord;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KaryawanDao extends PagingAndSortingRepository<Karyawan,String> {

    Long countKaryawanByStatus (StatusRecord statusRecord);
    Karyawan findByIdUser(User user);

}
