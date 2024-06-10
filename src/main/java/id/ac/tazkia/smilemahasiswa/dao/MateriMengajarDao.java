package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.Jadwal;
import id.ac.tazkia.smilemahasiswa.entity.MateriMengajar;
import id.ac.tazkia.smilemahasiswa.entity.SesiKuliah;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MateriMengajarDao extends PagingAndSortingRepository<MateriMengajar, String> {

    List<MateriMengajar> findByStatusAndSesiKuliah(StatusRecord status, SesiKuliah sesiKuliah);

    List<MateriMengajar> findByStatusAndSesiKuliahJadwal(StatusRecord status, Jadwal jadwal);

}
