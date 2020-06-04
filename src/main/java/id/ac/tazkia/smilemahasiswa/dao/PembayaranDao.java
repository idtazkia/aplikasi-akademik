package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface PembayaranDao extends PagingAndSortingRepository<Pembayaran, String> {

    Pembayaran findByTagihan(Tagihan tagihan);

}
