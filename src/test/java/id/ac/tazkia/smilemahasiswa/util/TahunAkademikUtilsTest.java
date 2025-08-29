package id.ac.tazkia.smilemahasiswa.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TahunAkademikUtilsTest {

    @Test
    void testParseAngkatan_TwoDigitFormat() {
        // Test 2-digit format for pascasarjana
        assertEquals(2017, TahunAkademikUtils.parseAngkatan("17"));
        assertEquals(2018, TahunAkademikUtils.parseAngkatan("18"));
        assertEquals(2000, TahunAkademikUtils.parseAngkatan("00"));
        assertEquals(2030, TahunAkademikUtils.parseAngkatan("30"));
        assertEquals(1931, TahunAkademikUtils.parseAngkatan("31"));
        assertEquals(1999, TahunAkademikUtils.parseAngkatan("99"));
    }

    @Test
    void testParseAngkatan_FourDigitFormat() {
        // Test 4-digit format for regular programs
        assertEquals(2017, TahunAkademikUtils.parseAngkatan("2017"));
        assertEquals(2018, TahunAkademikUtils.parseAngkatan("2018"));
        assertEquals(1995, TahunAkademikUtils.parseAngkatan("1995"));
    }

    @Test
    void testParseAngkatan_NullAndEmpty() {
        assertNull(TahunAkademikUtils.parseAngkatan(null));
        assertNull(TahunAkademikUtils.parseAngkatan(""));
        assertNull(TahunAkademikUtils.parseAngkatan("   "));
    }

    @Test
    void testParseAngkatan_InvalidFormat() {
        assertThrows(NumberFormatException.class, () -> {
            TahunAkademikUtils.parseAngkatan("abc");
        });
        assertThrows(NumberFormatException.class, () -> {
            TahunAkademikUtils.parseAngkatan("12.5");
        });
    }

    @Test
    void testIsOverlapping_NoOverlap() {
        // Test non-overlapping ranges
        assertFalse(TahunAkademikUtils.isOverlapping("2015", "2017", "2018", "2020"));
        assertFalse(TahunAkademikUtils.isOverlapping("2018", "2020", "2015", "2017"));
    }

    @Test
    void testIsOverlapping_WithOverlap() {
        // Test overlapping ranges
        assertTrue(TahunAkademikUtils.isOverlapping("2015", "2018", "2017", "2020"));
        assertTrue(TahunAkademikUtils.isOverlapping("2017", "2020", "2015", "2018"));
        assertTrue(TahunAkademikUtils.isOverlapping("2016", "2018", "2015", "2020"));
    }

    @Test
    void testIsOverlapping_MixedFormats() {
        // Test 2-digit vs 4-digit formats
        assertTrue(TahunAkademikUtils.isOverlapping("17", "18", "2017", "2019"));
        assertFalse(TahunAkademikUtils.isOverlapping("15", "16", "2017", "2019"));
    }

    @Test
    void testIsOverlapping_WithNulls() {
        // Test with null values (unlimited ranges)
        assertTrue(TahunAkademikUtils.isOverlapping(null, "2018", "2017", "2020"));
        assertTrue(TahunAkademikUtils.isOverlapping("2017", null, "2015", "2018"));
        assertTrue(TahunAkademikUtils.isOverlapping("2017", "2020", null, "2018"));
        assertTrue(TahunAkademikUtils.isOverlapping("2017", "2020", "2015", null));
    }

    @Test
    void testGenerateNextKode_NormalSemester() {
        assertEquals("20252", TahunAkademikUtils.generateNextKode("20251", "GANJIL", "2025"));
        assertEquals("20253", TahunAkademikUtils.generateNextKode("20252", "GENAP", "2025"));
    }

    @Test
    void testGenerateNextKode_PendekSemester() {
        assertEquals("20261", TahunAkademikUtils.generateNextKode("20253", "PENDEK", "2025"));
    }

    @Test
    void testGenerateNamaTahunAkademik() {
        assertEquals("2024/2025 Semester Ganjil",
                TahunAkademikUtils.generateNamaTahunAkademik("20241"));
        assertEquals("2024/2025 Semester Genap",
                TahunAkademikUtils.generateNamaTahunAkademik("20242"));
        assertEquals("2024/2025 Semester Pendek",
                TahunAkademikUtils.generateNamaTahunAkademik("20243"));
    }

    @Test
    void testGenerateNamaTahunAkademik_InvalidKode() {
        assertThrows(IllegalArgumentException.class, () -> {
            TahunAkademikUtils.generateNamaTahunAkademik("2024");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            TahunAkademikUtils.generateNamaTahunAkademik(null);
        });
    }

    @Test
    void testGetJenisSemester() {
        assertEquals("Ganjil", TahunAkademikUtils.getJenisSemester("1"));
        assertEquals("Genap", TahunAkademikUtils.getJenisSemester("2"));
        assertEquals("Pendek", TahunAkademikUtils.getJenisSemester("3"));
        assertEquals("Unknown", TahunAkademikUtils.getJenisSemester("4"));
    }

    @Test
    void testIsValidKodeTahunAkademik() {
        assertTrue(TahunAkademikUtils.isValidKodeTahunAkademik("20241"));
        assertTrue(TahunAkademikUtils.isValidKodeTahunAkademik("20242"));
        assertTrue(TahunAkademikUtils.isValidKodeTahunAkademik("20243"));

        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik("2024"));
        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik("202411"));
        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik("20244"));
        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik("18991"));
        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik("21011"));
        assertFalse(TahunAkademikUtils.isValidKodeTahunAkademik(null));
    }

    @Test
    void testExtractTahunFromKode() {
        assertEquals(2024, TahunAkademikUtils.extractTahunFromKode("20241"));
        assertEquals(2025, TahunAkademikUtils.extractTahunFromKode("20252"));
        assertNull(TahunAkademikUtils.extractTahunFromKode("invalid"));
        assertNull(TahunAkademikUtils.extractTahunFromKode(null));
    }

    @Test
    void testExtractJenisFromKode() {
        assertEquals("1", TahunAkademikUtils.extractJenisFromKode("20241"));
        assertEquals("2", TahunAkademikUtils.extractJenisFromKode("20252"));
        assertEquals("3", TahunAkademikUtils.extractJenisFromKode("20253"));
        assertNull(TahunAkademikUtils.extractJenisFromKode("invalid"));
        assertNull(TahunAkademikUtils.extractJenisFromKode(null));
    }
}
