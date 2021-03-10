package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import id.ac.tazkia.smilemahasiswa.entity.VirtualAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface VirtualAccountDao extends PagingAndSortingRepository<VirtualAccount, String> {
    Page<VirtualAccount> findByTagihan(Tagihan tagihan, Pageable pageable);

    @Query(value = "SELECT * FROM virtual_account where id_tagihan=?1 limit 2", nativeQuery = true)
    List<VirtualAccount> listVa(String idTagihan);

    @Query(value = "select * from virtual_account where id_bank=?1 and id_tagihan=?2 limit 1", nativeQuery = true)
    VirtualAccount vaPembayaran(String idBank, String idTagihan );

}
