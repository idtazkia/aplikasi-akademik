package id.ac.tazkia.smilemahasiswa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import id.ac.tazkia.smilemahasiswa.dao.DetailTahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

@Service
public class DetailTahunAkademikService {

    @Autowired
    private DetailTahunAkademikDao detailTahunAkademikDao;

    /**
     * Mendapatkan DetailTahunAkademik yang berlaku untuk mahasiswa tertentu
     * Hasil akan di-cache berdasarkan parameter input
     */
    @Cacheable(value = "detailTahunAkademik", key = "#tahunAkademik.id + '_' + #mahasiswa.idProdi.id + '_' + #mahasiswa.idProgram.id + '_' + #mahasiswa.angkatan")
    public DetailTahunAkademik getDetailTahunAkademikForMahasiswa(
            TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {

        List<DetailTahunAkademik> applicableRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademik.getId(),
                mahasiswa.getIdProdi().getId(),
                mahasiswa.getIdProgram().getId(),
                mahasiswa.getAngkatan(),
                StatusRecord.AKTIF);

        // Kembalikan rule dengan prioritas tertinggi (sudah di-order by prioritas DESC)
        return applicableRules.isEmpty() ? null : applicableRules.get(0);
    }

    /**
     * Method overload untuk kemudahan penggunaan
     */
    @Cacheable(value = "detailTahunAkademik", key = "#tahunAkademikId + '_' + #prodiId + '_' + #programId + '_' + #angkatan")
    public DetailTahunAkademik getDetailTahunAkademikForMahasiswa(
            String tahunAkademikId, String prodiId, String programId, String angkatan) {

        List<DetailTahunAkademik> applicableRules = detailTahunAkademikDao.findApplicableRules(
                tahunAkademikId,
                prodiId,
                programId,
                angkatan,
                StatusRecord.AKTIF);

        return applicableRules.isEmpty() ? null : applicableRules.get(0);
    }

    /**
     * Clear cache untuk specific mahasiswa dan tahun akademik
     */
    @CacheEvict(value = "detailTahunAkademik", key = "#tahunAkademik.id + '_' + #mahasiswa.idProdi.id + '_' + #mahasiswa.idProgram.id + '_' + #mahasiswa.angkatan")
    public void clearCacheForMahasiswa(TahunAkademik tahunAkademik, Mahasiswa mahasiswa) {
        // Method ini akan clear cache untuk mahasiswa dan tahun akademik tertentu
    }

    /**
     * Clear semua cache DetailTahunAkademik
     * Panggil method ini ketika ada update pada data DetailTahunAkademik
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public void clearAllCache() {
        // Method ini akan clear semua cache DetailTahunAkademik
    }

    /**
     * Method untuk save dengan auto clear cache
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public DetailTahunAkademik save(DetailTahunAkademik detailTahunAkademik) {
        return detailTahunAkademikDao.save(detailTahunAkademik);
    }

    /**
     * Method untuk delete dengan auto clear cache
     */
    @CacheEvict(value = "detailTahunAkademik", allEntries = true)
    public void delete(DetailTahunAkademik detailTahunAkademik) {
        detailTahunAkademikDao.delete(detailTahunAkademik);
    }

    /**
     * Method untuk mendapatkan semua DetailTahunAkademik berdasarkan tahun akademik
     * Juga di-cache karena sering digunakan
     */
    @Cacheable(value = "detailTahunAkademikByTahun", key = "#tahunAkademik.id")
    public List<DetailTahunAkademik> findByTahunAkademik(TahunAkademik tahunAkademik) {
        return detailTahunAkademikDao.findByTahunAkademikAndStatus(tahunAkademik, StatusRecord.AKTIF);
    }

    /**
     * Clear cache untuk tahun akademik tertentu
     */
    @CacheEvict(value = "detailTahunAkademikByTahun", key = "#tahunAkademik.id")
    public void clearCacheForTahunAkademik(TahunAkademik tahunAkademik) {
        // Method ini akan clear cache untuk tahun akademik tertentu
    }
}
