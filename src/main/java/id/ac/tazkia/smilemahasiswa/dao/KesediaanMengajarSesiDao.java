package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.KesediaanMengajarDosen;
import id.ac.tazkia.smilemahasiswa.entity.KesediaanMengajarSesi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KesediaanMengajarSesiDao extends PagingAndSortingRepository<KesediaanMengajarSesi, String> {

    List<KesediaanMengajarSesi> findByKesediaanDosenAndStatus(KesediaanMengajarDosen kesediaanMengajarDosen, StatusRecord statusRecord);

}
