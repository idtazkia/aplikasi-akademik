package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.JenisPembinaanMatrikulasi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PembinaanDao extends PagingAndSortingRepository<JenisPembinaanMatrikulasi, String> {

    List<JenisPembinaanMatrikulasi> findByStatus(StatusRecord statusRecord);

}
