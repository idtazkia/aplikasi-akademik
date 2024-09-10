package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Angkatan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AngkatanDao extends JpaRepository<Angkatan, String> {


}
