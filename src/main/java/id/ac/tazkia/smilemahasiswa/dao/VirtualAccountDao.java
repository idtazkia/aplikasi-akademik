package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import id.ac.tazkia.smilemahasiswa.entity.VirtualAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface VirtualAccountDao extends PagingAndSortingRepository<VirtualAccount, String> {
    Page<VirtualAccount> findByTagihan(Tagihan tagihan, Pageable pageable);

}
