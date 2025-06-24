package id.ac.tazkia.smilemahasiswa.dao.edom;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.edom.EdomEvaluationTarget;

public interface EdomEvaluationTargetDao extends PagingAndSortingRepository<EdomEvaluationTarget, String> {
    Optional<EdomEvaluationTarget> findByMahasiswaAndTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);
}
