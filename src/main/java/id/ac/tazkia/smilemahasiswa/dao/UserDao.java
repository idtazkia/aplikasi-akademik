package id.ac.tazkia.smilemahasiswa.dao;


import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserDao extends PagingAndSortingRepository<User, String> {
    User findByUsername(String username);

}
