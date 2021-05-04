package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.*;
import org.bouncycastle.ocsp.Req;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.lang.ref.ReferenceQueue;
import java.time.LocalDate;
import java.util.List;

public interface RequestPenangguhanDao extends PagingAndSortingRepository<RequestPenangguhan, String> {

    Page<RequestPenangguhan> findByStatusNotInAndTanggalPenangguhanContainingIgnoreCaseOrderByTanggalPenangguhan(List<StatusRecord> asList, String search, Pageable page);

    Page<RequestPenangguhan> findByStatusNotInOrderByTanggalPengajuanDesc(List<StatusRecord> asList, Pageable page);

    RequestPenangguhan findByTagihanAndStatusAndStatusApprove(Tagihan tagihan, StatusRecord statusRecord, StatusApprove statusApprove);

    List<RequestPenangguhan> findByStatusAndStatusApproveAndTanggalPenangguhan(StatusRecord statusRecord, StatusApprove statusApprove, LocalDate penangguhan);

    @Query(value = "select b.id, e.nama, a.status_approve as status, a.keterangan_reject as keterangan from tagihan as b inner join mahasiswa as c on b.id_mahasiswa=c.id inner join nilai_jenis_tagihan as d on b.id_nilai_jenis_tagihan=d.id inner join jenis_tagihan as e on d.id_jenis_tagihan=e.id inner join tahun_akademik as f on b.id_tahun_akademik=f.id left join request_penangguhan as a on a.id_tagihan=b.id where a.status='AKTIF' and c.nim=?1 and f.id=?2", nativeQuery = true)
    List<Object[]> cekPenangguhanPerMahasiswa(Mahasiswa mahasiswa, TahunAkademik tahunAkademik);

}
