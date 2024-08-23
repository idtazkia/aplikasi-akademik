package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.RequestKonversiTenggat;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RequestKonversiTenggatDao extends PagingAndSortingRepository<RequestKonversiTenggat,String> {

    RequestKonversiTenggat findByStatus(StatusRecord statusRecord);

}
