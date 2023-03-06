package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Ayah;
import id.ac.tazkia.smilemahasiswa.entity.User;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface AyahDao extends PagingAndSortingRepository<Ayah, String> {
    Ayah findByUser(User user);
}
