package id.ac.tazkia.smilemahasiswa.dao;


import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface UserDao extends PagingAndSortingRepository<User, String> {
    User findByUsername(String username);
    User findByUsernameAndId(String username,String id);

}
