package id.ac.tazkia.akademik.aplikasiakademik.dao;

import id.ac.tazkia.akademik.aplikasiakademik.dto.RekapMissAttendance;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RekapKehadiranMahasiswaDao extends PagingAndSortingRepository<RekapKehadiranMahasiswa, String> {
    RekapKehadiranMahasiswa findByMahasiswaAndJadwal(Mahasiswa mahasiswa, Jadwal jadwal);


    @Query("select new id.ac.tazkia.akademik.aplikasiakademik.dto.MahasiswaAttendance(p.id, p.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah, coalesce(p.hadir,0), coalesce(p.mangkir,0),coalesce(p.izin,0)) " +
            "from RekapKehadiranMahasiswa p where p.jadwal.tahunAkademik = :tahunAkademik and p.mahasiswa = :mahasiswa " +
            "order by p.jadwal.matakuliahKurikulum.matakuliah.namaMatakuliah")
    List<RekapKehadiranMahasiswa> rekapKehadiranMahasiswa(@Param("tahunAkademik")TahunAkademik tahunAkademik,@Param("mahasiswa")Mahasiswa mahasiswa);

}
