package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import id.ac.tazkia.akademik.aplikasiakademik.entity.UserPassword;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserPasswordDao extends PagingAndSortingRepository<UserPassword, String> {
    UserPassword findByUser(User user);
}
