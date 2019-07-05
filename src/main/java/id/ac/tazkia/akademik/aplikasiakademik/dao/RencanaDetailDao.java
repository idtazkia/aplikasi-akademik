package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.RencanaDetail;
import id.ac.tazkia.akademik.aplikasiakademik.entity.RencanaPembayaran;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface RencanaDetailDao extends PagingAndSortingRepository<RencanaDetail, String> {
    RencanaDetail findByRencanaPembayaran(RencanaPembayaran rencanaPembayaran);
    List<RencanaDetail> findByRencanaPembayaranAndKeteranganIsNull(RencanaPembayaran rencanaPembayaran);
}
