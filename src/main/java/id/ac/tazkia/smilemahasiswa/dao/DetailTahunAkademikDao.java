package id.ac.tazkia.smilemahasiswa.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;

import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

@Repository
public interface DetailTahunAkademikDao extends JpaRepository<DetailTahunAkademik, String> {

        /**
         * Query native untuk mendapatkan kombinasi prodi-program
         */
        @Query(value = "SELECT p.id as id_prodi, p.nama_prodi, pr.id as id_program, pr.nama_program " +
                        "FROM prodi p " +
                        "JOIN prodi_program pp ON p.id = pp.id_prodi " +
                        "JOIN program pr ON pp.id_program = pr.id " +
                        "WHERE p.status = 'AKTIF' AND pr.status = 'AKTIF' " +
                        "ORDER BY p.nama_prodi, pr.nama_program", nativeQuery = true)
        List<Object[]> findAllActiveProdiProgramCombinations();

        /**
         * Cari detail berdasarkan tahun akademik
         */
        List<DetailTahunAkademik> findByTahunAkademikAndStatus(TahunAkademik tahunAkademik, StatusRecord status);

        /**
         * Cari detail yang berlaku untuk mahasiswa tertentu
         */
        @Query("SELECT d FROM DetailTahunAkademik d " +
                        "WHERE d.tahunAkademik.id = :tahunAkademikId " +
                        "AND (d.prodi.id = :prodiId OR d.prodi IS NULL) " +
                        "AND (d.program.id = :programId OR d.program IS NULL) " +
                        "AND (d.angkatanMulai IS NULL OR :angkatan >= d.angkatanMulai) " +
                        "AND (d.angkatanSelesai IS NULL OR :angkatan <= d.angkatanSelesai) " +
                        "AND d.status = :status " +
                        "ORDER BY d.prioritas DESC, d.prodi.id DESC, d.program.id DESC")
        List<DetailTahunAkademik> findApplicableRules(
                        @Param("tahunAkademikId") String tahunAkademikId,
                        @Param("prodiId") String prodiId,
                        @Param("programId") String programId,
                        @Param("angkatan") String angkatan,
                        @Param("status") StatusRecord status);

        /**
         * Query untuk validasi dinamis - cari data existing berdasarkan prodi dan
         * program
         * dengan urutan angkatan mulai ascending untuk mencari minimum
         */
        List<DetailTahunAkademik> findByProdiAndProgramAndStatusOrderByAngkatanMulaiAsc(
                        Prodi prodi,
                        Program program,
                        StatusRecord status);

        /**
         * Query untuk validasi dinamis - cari data existing berdasarkan prodi dan
         * program
         * dengan urutan angkatan selesai descending untuk mencari maksimum
         */
        List<DetailTahunAkademik> findByProdiAndProgramAndStatusOrderByAngkatanSelesaiDesc(
                        Prodi prodi,
                        Program program,
                        StatusRecord status);

        List<DetailTahunAkademik> findByTahunAkademikAndProdiAndStatus(TahunAkademik tahunAkademik, Prodi prodi, StatusRecord status);
}
