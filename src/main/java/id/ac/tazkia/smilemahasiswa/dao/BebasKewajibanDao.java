package id.ac.tazkia.smilemahasiswa.dao;

import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.ByBagian;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.PendaftaranBebasKewajiban;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BebasKewajibanDao extends PagingAndSortingRepository<PendaftaranBebasKewajiban, String> {

    PendaftaranBebasKewajiban findByMahasiswa(Mahasiswa mahasiswa);

    PendaftaranBebasKewajiban findByMahasiswaAndStatus(Mahasiswa mahasiswa, StatusRecord statusRecord);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApprovePerpusNotOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, Pageable page);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveKabagPerpusNotOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveKeuanganNotOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveTlcNotOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveAkademikNotOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, Pageable page);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApprovePerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);

    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveKabagPerpusNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveKeuanganNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveTlcNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);
    Page<PendaftaranBebasKewajiban> findByStatusAndStatusApproveAkademikNotAndMahasiswaNamaContainingIgnoreCaseOrMahasiswaNimContainingIgnoreCaseOrderByTanggalRequest(StatusRecord status, StatusApprove statusApprove, String nama, String nim, Pageable page);

    @Query(value = "select pb.id, m.nim, m.nama, p.nama_prodi, m.angkatan, hb.waktu_input as waktu, hb.status_approve, u.username from pendaftaran_bebas_kewajiban pb inner join histori_bebas_kewajiban hb on pb.id=hb.id_pendaftaran inner join mahasiswa m on pb.id_mahasiswa=m.id inner join prodi p on m.id_prodi=p.id inner join s_user u on hb.user_modified=u.id where hb.status_approve='APPROVED' and by_bagian=?1 group by pb.id_mahasiswa order by waktu_input desc", nativeQuery = true)
    List<Object[]> historiApproval(String bagian);

}
