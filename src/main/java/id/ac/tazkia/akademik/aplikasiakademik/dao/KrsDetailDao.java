package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.SqlResultSetMapping;
import java.util.List;

public interface KrsDetailDao extends PagingAndSortingRepository<KrsDetail,String> {
   Page<KrsDetail> findByKrsAndMahasiswaOrderByJadwalIdHariAscJadwalJamMulaiAsc(Krs krs, Mahasiswa mahasiswa, Pageable page);

    @Query("SELECT u FROM KrsDetail u WHERE u.mahasiswa = ?1 and u.krs = ?2 and u.status= ?3 and u.jadwal.idHari in(DAYOFWEEK(NOW())-1,DAYOFWEEK(NOW())) order by u.jadwal.idHari,u.jadwal.jamMulai")
    List<KrsDetail> findByMahasiswaAndKrsAndStatus(Mahasiswa mahasiswa, Krs krs, StatusRecord statusRecord);

   Page<KrsDetail> findByMahasiswaAndKrsTahunAkademik(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, Pageable page);

    Page<KrsDetail> findByKrsAndMahasiswa(Krs krs, Mahasiswa mahasiswa, Pageable page);

    List<KrsDetail> findByMahasiswaAndStatusOrderByKrsTahunAkademikDesc(Mahasiswa mahasiswa,StatusRecord statusRecord);


/*
    @SqlResultSetMapping(
            name="groupDetailsMapping",
            classes={
                    @ConstructorResult(
                            targetClass=id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaAttendance.class,
                            columns={
                                    @ColumnResult(name="GROUP_ID"),
                                    @ColumnResult(name="USER_ID")
                            }
                    )
            }
    )

    @NamedNativeQuery(name="getGroupDetails", query=" select c.id, f.nama_matakuliah, count(a.id) " +
            "from " +
            "(select * from Presensi_Mahasiswa where status = 'AKTIF' and status_presensi not in ('H'))a inner join " +
            "(select id,id_krs,id_jadwal from krs_detail)b on a.id_krs_detail=b.id inner join " +
            "(select id,id_matakuliah_kurikulum from jadwal)c on b.id_jadwal=c.id inner join " +
            "(select id from krs where id= ?1)d on b.id_krs=d.id inner join " +
            "(select id,id_matakuliah from matakuliah_kurikulum)e on c.id_matakuliah_kurikulum=e.id inner join " +
            "(select * from matakuliah)f on e.id_matakuliah=f.id " +
            "group by a.id_krs_detail " +
            "order by f.nama_matakuliah",
            resultSetMapping="groupDetailsMapping");
*/



}
