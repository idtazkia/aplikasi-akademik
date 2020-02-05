package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Role;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RoleDao extends PagingAndSortingRepository<Role,String> {
}
