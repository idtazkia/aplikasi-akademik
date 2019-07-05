package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.RencanaPembayaran;
import id.ac.tazkia.akademik.aplikasiakademik.entity.TagihanMahasiswa;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RencanaPembayaranDao extends PagingAndSortingRepository<RencanaPembayaran, String> {
    RencanaPembayaran findByTagihanMahasiswa(TagihanMahasiswa tagihanMahasiswa);
}
