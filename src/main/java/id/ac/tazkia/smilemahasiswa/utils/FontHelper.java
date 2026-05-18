package id.ac.tazkia.smilemahasiswa.utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Helper class untuk font handling dengan fallback mechanism
 * untuk menghindari error font yang tidak tersedia di production
 */
public class FontHelper {

    private static final Logger logger = LoggerFactory.getLogger(FontHelper.class);

    /**
     * Mendapatkan font Times-Roman dengan fallback ke Helvetica
     * jika Times-Roman tidak tersedia.
     *
     * @param size ukuran font
     * @return Font object (Times-Roman atau Helvetica sebagai fallback)
     */
    public static Font getSafeFont(int size) {
        return getSafeFont(size, Font.NORMAL);
    }

    /**
     * Mendapatkan font Times-Roman dengan fallback ke Helvetica
     * jika Times-Roman tidak tersedia.
     *
     * @param size ukuran font
     * @param style style font (NORMAL, BOLD, ITALIC, etc)
     * @return Font object (Times-Roman atau Helvetica sebagai fallback)
     */
    public static Font getSafeFont(int size, int style) {
        try {
            // Coba gunakan Times-Roman dulu
            Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, size, style);
            // Test apakah font valid dengan mencoba mendapatkan basefont
            if (font.getBaseFont() != null) {
                return font;
            }
        } catch (Exception e) {
            logger.warn("Times-Roman font tidak tersedia, menggunakan fallback ke Helvetica. Error: {}",
                    e.getMessage());
        }

        // Fallback ke Helvetica yang selalu tersedia
        logger.debug("Menggunakan Helvetica sebagai fallback font");
        return FontFactory.getFont(FontFactory.HELVETICA, size, style);
    }

    /**
     * Mendapatkan font Times-Roman dengan encoding dan fallback ke Helvetica
     *
     * @param size ukuran font
     * @param style style font
     * @param color warna font
     * @return Font object (Times-Roman atau Helvetica sebagai fallback)
     */
    public static Font getSafeFont(int size, int style, java.awt.Color color) {
        try {
            Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, size, style, color);
            if (font.getBaseFont() != null) {
                return font;
            }
        } catch (Exception e) {
            logger.warn("Times-Roman font tidak tersedia, menggunakan fallback ke Helvetica. Error: {}",
                    e.getMessage());
        }

        return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
    }
}
