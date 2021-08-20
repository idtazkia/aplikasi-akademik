package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.JenisKelamin;
import id.ac.tazkia.smilemahasiswa.entity.KuotaTagihanOffline;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface KuotaOfflineDao extends PagingAndSortingRepository<KuotaTagihanOffline, String> {

    KuotaTagihanOffline findByAngkatanAndTahunAkademik(String angkatan, TahunAkademik tahunAkademik);

    KuotaTagihanOffline findByAngkatanAndJenisKelaminAndTahunAkademik(String angkatan, JenisKelamin jk, TahunAkademik tahunAkademik);

}
