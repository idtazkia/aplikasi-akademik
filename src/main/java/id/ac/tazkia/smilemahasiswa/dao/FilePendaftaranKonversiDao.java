package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.FilePendaftaranKonversi;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranRequestKonversi;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface FilePendaftaranKonversiDao extends PagingAndSortingRepository<FilePendaftaranKonversi, String> {
    FilePendaftaranKonversi findByPendaftaranRequestKonversi(PendaftaranRequestKonversi pendaftaranRequestKonversi);
}
