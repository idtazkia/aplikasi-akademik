package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Dosen;
import id.ac.tazkia.smilemahasiswa.entity.KesediaanMengajarDosen;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface KesediaanMengajarDosenDao extends PagingAndSortingRepository<KesediaanMengajarDosen, String> {

    KesediaanMengajarDosen findByDosenAndTahunAkademikAndStatus(Dosen dosen, TahunAkademik tahunAkademik, StatusRecord statusRecord);

}
