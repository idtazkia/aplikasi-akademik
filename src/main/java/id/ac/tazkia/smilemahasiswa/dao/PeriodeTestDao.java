package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.constant.JenisTest;
import id.ac.tazkia.smilemahasiswa.dto.periodetest.GetData;
import id.ac.tazkia.smilemahasiswa.entity.PeriodeTest;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface PeriodeTestDao extends PagingAndSortingRepository<PeriodeTest, String> {
    @Query(value = " select pt.id as id, pt.tanggal_ujian as tanggal, pt.terakhir_upload as terakhir,pt.status as statuspt, pt.kuota as kuota,pt.jenis_test as jenis, pt.status as status, r.id as ruanganid,r.nama_ruangan as nama, (select count(*) from prediksi_test where id_periode = pt.id and status = 'AKTIF' and status_ujian not in ('REJECTED')) as total, r.id as ruanganId, r.nama_ruangan as ruangan from periode_test as pt inner join ruangan as r on pt.id_ruangan = r.id where pt.status in ('NONAKTIF','AKTIF')",
            countQuery ="select count(*) from periode_test as pt where status in ('NONAKTIF','AKTIF')" , nativeQuery = true)
    Page<GetData> listPEriode(Pageable pageable);

    Page<PeriodeTest> findByStatusInOrderByStatusAsc(List<StatusRecord> statusRecords, Pageable pageable);

    PeriodeTest findByStatusAndJenisTest(StatusRecord statusRecord, JenisTest jenisTest);

    List<PeriodeTest> findAllByJenisTestOrderByTanggalUjianDesc(JenisTest jenisTest);

    List<PeriodeTest> findAllByOrderByTanggalUjianDesc();
}
