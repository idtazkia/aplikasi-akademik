package id.ac.tazkia.smilemahasiswa.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import id.ac.tazkia.smilemahasiswa.dao.DetailTahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.DetailTahunAkademik;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.Program;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@SpringJUnitConfig
class DetailTahunAkademikServiceTest {

    @Mock
    private DetailTahunAkademikDao detailTahunAkademikDao;

    @InjectMocks
    private DetailTahunAkademikService detailTahunAkademikService;

    private CacheManager cacheManager;
    private TahunAkademik tahunAkademik;
    private Mahasiswa mahasiswa;
    private DetailTahunAkademik detailTahunAkademik;

    @BeforeEach
    void setUp() {
        // Setup cache manager
        cacheManager = new ConcurrentMapCacheManager("detailTahunAkademik", "detailTahunAkademikByTahun");

        // Setup test data
        tahunAkademik = new TahunAkademik();
        tahunAkademik.setId("ta-2025");
        tahunAkademik.setNamaTahunAkademik("2025/2026 Ganjil");

        Prodi prodi = new Prodi();
        prodi.setId("prodi-1");
        prodi.setNamaProdi("Teknik Informatika");

        Program program = new Program();
        program.setId("program-1");
        program.setNamaProgram("S1");

        mahasiswa = new Mahasiswa();
        mahasiswa.setId("mhs-1");
        mahasiswa.setIdProdi(prodi);
        mahasiswa.setIdProgram(program);
        mahasiswa.setAngkatan("2023");

        detailTahunAkademik = new DetailTahunAkademik();
        detailTahunAkademik.setId("detail-1");
        detailTahunAkademik.setTahunAkademik(tahunAkademik);
        detailTahunAkademik.setProdi(prodi);
        detailTahunAkademik.setProgram(program);
        detailTahunAkademik.setAngkatanMulai("2020");
        detailTahunAkademik.setAngkatanSelesai("2025");
        detailTahunAkademik.setTanggalMulaiKrs(LocalDate.of(2025, 1, 15));
        detailTahunAkademik.setTanggalSelesaiKrs(LocalDate.of(2025, 1, 25));
        detailTahunAkademik.setPrioritas(1);
        detailTahunAkademik.setStatus(StatusRecord.AKTIF);
    }

    @Test
    void testGetDetailTahunAkademikForMahasiswa_ShouldReturnCorrectDetail() {
        // Given
        when(detailTahunAkademikDao.findApplicableRules(
                anyString(), anyString(), anyString(), anyString(), any(StatusRecord.class)))
                .thenReturn(Arrays.asList(detailTahunAkademik));

        // When
        DetailTahunAkademik result = detailTahunAkademikService
                .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);

        // Then
        assertNotNull(result);
        assertEquals("detail-1", result.getId());
        assertEquals(LocalDate.of(2025, 1, 15), result.getTanggalMulaiKrs());
        assertEquals(LocalDate.of(2025, 1, 25), result.getTanggalSelesaiKrs());

        // Verify DAO was called
        verify(detailTahunAkademikDao).findApplicableRules(
                "ta-2025", "prodi-1", "program-1", "2023", StatusRecord.AKTIF);
    }

    @Test
    void testGetDetailTahunAkademikForMahasiswa_WhenNoRuleFound_ShouldReturnNull() {
        // Given
        when(detailTahunAkademikDao.findApplicableRules(
                anyString(), anyString(), anyString(), anyString(), any(StatusRecord.class)))
                .thenReturn(Collections.emptyList());

        // When
        DetailTahunAkademik result = detailTahunAkademikService
                .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);

        // Then
        assertNull(result);
    }

    @Test
    void testCacheWorking_ShouldCallDaoOnlyOnce() {
        // Given
        when(detailTahunAkademikDao.findApplicableRules(
                anyString(), anyString(), anyString(), anyString(), any(StatusRecord.class)))
                .thenReturn(Arrays.asList(detailTahunAkademik));

        // When - call the same method twice with same parameters
        DetailTahunAkademik result1 = detailTahunAkademikService
                .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);
        DetailTahunAkademik result2 = detailTahunAkademikService
                .getDetailTahunAkademikForMahasiswa(tahunAkademik, mahasiswa);

        // Then
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());

        // DAO should be called only once due to caching
        verify(detailTahunAkademikDao, times(1)).findApplicableRules(
                anyString(), anyString(), anyString(), anyString(), any(StatusRecord.class));
    }

    @Test
    void testSaveWithCacheEviction_ShouldClearCache() {
        // Given
        when(detailTahunAkademikDao.save(any(DetailTahunAkademik.class)))
                .thenReturn(detailTahunAkademik);

        // When
        DetailTahunAkademik result = detailTahunAkademikService.save(detailTahunAkademik);

        // Then
        assertNotNull(result);
        assertEquals("detail-1", result.getId());
        verify(detailTahunAkademikDao).save(detailTahunAkademik);
    }

    @Test
    void testFindByTahunAkademik_ShouldReturnList() {
        // Given
        when(detailTahunAkademikDao.findByTahunAkademikAndStatus(
                any(TahunAkademik.class), any(StatusRecord.class)))
                .thenReturn(Arrays.asList(detailTahunAkademik));

        // When
        var result = detailTahunAkademikService.findByTahunAkademik(tahunAkademik);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("detail-1", result.get(0).getId());
        verify(detailTahunAkademikDao).findByTahunAkademikAndStatus(tahunAkademik, StatusRecord.AKTIF);
    }
}
