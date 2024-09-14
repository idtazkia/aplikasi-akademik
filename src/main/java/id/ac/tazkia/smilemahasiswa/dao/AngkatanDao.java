package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Angkatan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface AngkatanDao extends JpaRepository<Angkatan, String>, PagingAndSortingRepository<Angkatan, String> {

    Angkatan findByAngkatan(String angkatan);
}
