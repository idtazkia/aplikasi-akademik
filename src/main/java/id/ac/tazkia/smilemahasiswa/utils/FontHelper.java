package id.ac.tazkia.smilemahasiswa.utils;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FontHelper {

    private static final Logger logger = LoggerFactory.getLogger(FontHelper.class);

    public static Font getSafeFont(int size) {
        return getSafeFont(size, Font.NORMAL);
    }

    public static Font getSafeFont(int size, int style) {
        try {
            // BaseFont.createFont() lebih reliable daripada FontFactory untuk font standar PDF
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, style);
        } catch (Exception e) {
            logger.warn("Times-Roman tidak tersedia, fallback ke Helvetica. Error: {}", e.getMessage());
        }
        return FontFactory.getFont(FontFactory.HELVETICA, size, style);
    }

    public static Font getSafeFont(int size, int style, java.awt.Color color) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            Font font = new Font(bf, size, style);
            font.setColor(color);
            return font;
        } catch (Exception e) {
            logger.warn("Times-Roman tidak tersedia, fallback ke Helvetica. Error: {}", e.getMessage());
        }
        return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
    }

    public static Font getSafeFontBold(int size) {
        try {
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            return new Font(bf, size, Font.NORMAL);
        } catch (Exception e) {
            logger.warn("Times-Bold tidak tersedia, fallback ke Helvetica-Bold. Error: {}", e.getMessage());
        }
        return FontFactory.getFont(FontFactory.HELVETICA_BOLD, size);
    }
}
