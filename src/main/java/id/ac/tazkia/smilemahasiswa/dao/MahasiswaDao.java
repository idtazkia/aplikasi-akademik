package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import id.ac.tazkia.smilemahasiswa.dto.ListAngkatanDto;
import id.ac.tazkia.smilemahasiswa.dto.machine.RfidDto;
import id.ac.tazkia.smilemahasiswa.entity.Ayah;
import id.ac.tazkia.smilemahasiswa.entity.Beasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Ibu;
import id.ac.tazkia.smilemahasiswa.entity.Jenjang;
import id.ac.tazkia.smilemahasiswa.entity.Konsentrasi;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;

public interface MahasiswaDao extends PagingAndSortingRepository<Mahasiswa, String> {
        Mahasiswa findByUser(User user);

        @Query("select distinct m.angkatan from Mahasiswa m order by m.angkatan asc")
        Iterable<Mahasiswa> cariAngkatan();

        @Query(value = "SELECT DISTINCT angkatan FROM mahasiswa m  inner join prodi p on m.id_prodi = p.id WHERE p.id_jenjang = :idJenjang ORDER BY angkatan DESC", nativeQuery = true)
        List<String> findDistinctAngkatanByJenjang(@Param("idJenjang") String idJenjang);

        /**
         * Get all distinct angkatan without jenjang filter
         * Sorting: 4-digit years first (DESC), then 2-digit years (DESC)
         * e.g., 2025, 2024, 2023, 2022, 2021, 25, 24, 23, 22, 21
         */
        @Query(value = "SELECT DISTINCT angkatan FROM mahasiswa " +
                        "ORDER BY LENGTH(angkatan) DESC, angkatan DESC", nativeQuery = true)
        List<String> findAllDistinctAngkatan();

        Mahasiswa findByIbu(Ibu ibu);

        Mahasiswa findByAyah(Ayah ayah);

        Mahasiswa findByNim(String nim);

        Mahasiswa findByNimAndStatus(String nim, StatusRecord status);

        @Query(value = "select id_absen as idAbsen,nama,rfid,'true' as sukses,'' as pesanError,(select count(*) from mahasiswa where status = 'AKTIF' and status_aktif = 'AKTIF' and rfid is not null) as jumlah from mahasiswa where status = 'AKTIF' and status_aktif = 'AKTIF' and rfid is not null;", nativeQuery = true)
        List<RfidDto> rfidMahasiswa();

        @Query("select m.id from Mahasiswa m where m.nim = :nim")
        String cariIdMahasiswa(@Param("nim") String nim);

        @Query("select m from Mahasiswa m where m.status = :status and m.angkatan = :angkatan and m.idProdi = :prodi")
        Iterable<Mahasiswa> carikelas(@Param("status") StatusRecord statusRecord, @Param("angkatan") String angkatan,
                        @Param("prodi") Prodi prodi);

        @Query("select m from Mahasiswa m where m.status = :status and m.angkatan = :angkatan and m.idProdi = :prodi and m.idKonsentrasi = :konsentrasi")
        Iterable<Mahasiswa> carikelasKonsentrai(@Param("status") StatusRecord statusRecord,
                        @Param("angkatan") String angkatan, @Param("prodi") Prodi prodi,
                        @Param("konsentrasi") Konsentrasi konsentrasi);

        List<Mahasiswa> findByStatusAndAngkatan(StatusRecord aktif, String angkatan);

        @Query(value = "select angkatan,nama_jenjang from mahasiswa as a inner join prodi as b on a.id_prodi = b.id inner join jenjang as c on b.id_jenjang = c.id group by concat(angkatan,id_jenjang) order by angkatan desc", nativeQuery = true)
        List<Object> angkatanMahasiswa();

        Page<Mahasiswa> findByStatusNotInAndNamaContainingIgnoreCaseOrNimOrderByNim(List<StatusRecord> statusRecords,
                        String nama, String nim, Pageable page);

        Page<Mahasiswa> findByStatusNotInOrderByNim(List<StatusRecord> statusRecords, Pageable page);

        @Query(value = "SELECT MAX(id_absen) FROM mahasiswa;", nativeQuery = true)
        Integer cariMaxAbsen();

        List<Mahasiswa> findByStatusAndIdProdi(StatusRecord statusRecord, Prodi prodi);

        Page<Mahasiswa> findByAngkatanAndIdProdiAndStatus(String angkatan, Prodi prodi, StatusRecord statusRecord,
                        Pageable page);

        List<Mahasiswa> findByStatusAndAngkatanAndIdProdiAndIdProgram(StatusRecord aktif, String angkatan, Prodi prodi,
                        Program program);

        List<Mahasiswa> findByIdProdiAndStatus(Prodi prodi, StatusRecord statusRecord);

        List<Mahasiswa> findByIdProdiAndIdProgramAndAngkatanAndStatusAktifAndStatusAndBeasiswaIsNull(Prodi prodi,
                        Program program, String angkatan, String s, StatusRecord statusRecord);

        List<Mahasiswa> findByIdProdiAndIdProgramAndAngkatanAndStatusAndStatusAktifAndBeasiswaIsNotNull(Prodi prodi,
                        Program program, String angkatan, StatusRecord statusRecord, String status);

        @Query(value = "select a.angkatan,c.nama_jenjang as namaJenjang from mahasiswa as a\n" +
                        "inner join prodi as b on a.id_prodi = b.id\n" +
                        "inner join jenjang as c on b.id_jenjang = c.id \n" +
                        "where a.status = 'AKTIF' and a.status_aktif = 'AKTIF' group by a.angkatan order by a.angkatan\n", nativeQuery = true)
        List<ListAngkatanDto> listAngkatanMahasiswa();

        List<Mahasiswa> findByStatusAndStatusAktifAndBeasiswaIsNull(StatusRecord statusRecord, String status);

        List<Mahasiswa> findByStatusAndStatusAktif(StatusRecord statusRecord, String status);

        List<Mahasiswa> findByStatusAndStatusAktifIn(StatusRecord statusRecord, List<String> status);

        List<Mahasiswa> findByIdProdiIdJenjangAndStatusAndStatusAktifIn(Jenjang idJenjang, StatusRecord statusRecord,
                        List<String> status);

        List<Mahasiswa> findByStatusAndStatusAktifAndAngkatan(StatusRecord statusRecord, String status,
                        String angkatan);

        @Query(value = "UPDATE mahasiswa SET id_absen= FLOOR(RAND() * (99999 - 77777) + 9999)", nativeQuery = true)
        Update updataIdAbsenMahasiswa();

        @Query(value = "select * from mahasiswa where angkatan = ?1 and tanggal_disahkan is not null limit 1", nativeQuery = true)
        Mahasiswa cekTanggalDisahkan(String angkatan);

        List<Mahasiswa> findByBeasiswaAndStatusAktifAndStatusAndAngkatanAndIdProgram(Beasiswa beasiswa, String status,
                        StatusRecord statusRecord, String angkatan, Program program);

        @Query(value = "select nama_prodi,angkatan, nim, nama,jenis_kelamin, if(sks is not null, if(sks > 0, 'AKTIF','NONAKTIF'),'NONAKTIF') as status_semester, \n"
                        +
                        "coalesce(if(sks is not null, if(sks > 0, 'AKTIF',null),null),status_aktif) as status,coalesce(nama_tahun_akademik, '-') as nama_tahun, coalesce(sks, '-') as sks, coalesce(ipk, '-') as ipk, nama_program, coalesce(nama_beasiswa, '-') as beasiswa from\n"
                        +
                        "(select a.*, nama_prodi, nama_program, ipk from mahasiswa as a\n" +
                        "inner join prodi as b on a.id_prodi = b.id\n" +
                        "inner join program as p on a.id_program=p.id\n" +
                        "left join ipk as c on a.id = c.id_mahasiswa\n" +
                        "where angkatan in (?1) and a.status = 'AKTIF') as a\n" +
                        "left join\n" +
                        "(select a.id_mahasiswa, sum(jumlah_sks) as sks, nama_tahun_akademik from krs as a\n" +
                        "inner join krs_detail as b on a.id = b.id_krs \n" +
                        "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
                        "inner join tahun_akademik as d on a.id_tahun_akademik = d.id\n" +
                        "where a.id_tahun_akademik = ?2 and a.status = 'AKTIF'\n" +
                        "and b.status = 'AKTIF' group by id_krs) as b\n" +
                        "on a.id = b.id_mahasiswa \n" +
                        "left join \n" +
                        "(select a.id_mahasiswa, b.nama_beasiswa from mahasiswa_beasiswa as a inner join beasiswa as b on a.id_beasiswa=b.id) as c \n"
                        +
                        "on a.id=c.id_mahasiswa \n" +
                        "order by angkatan,id_prodi, nim", nativeQuery = true)
        List<Object[]> listStatusMahasiswa(List<String> angkatan, String idTahunAkademik);

        @Query("select m from Mahasiswa m where m.dosen.karyawan.idUser = :user and m.status='AKTIF' and m.statusAktif = 'AKTIF' order by m.angkatan, m.idProdi.namaProdi")
        List<Mahasiswa> listMahasiswaByDosenWali(@Param("user") User user);

        @Query(value = "select if(coalesce(sum(jumlah_sks),0)=0,'NONAKTIF','AKTIF') as ada from krs as a\n" +
                        "inner join krs_detail as b on a.id = b.id_krs and b.status = 'AKTIF'\n" +
                        "inner join matakuliah_kurikulum as c on b.id_matakuliah_kurikulum = c.id\n" +
                        "where a.id_tahun_akademik = ?1 and a.id_mahasiswa = ?2", nativeQuery = true)
        StatusRecord stausAktifMahasiswa(String tahunAkademik, String idMahasiswa);
}
