package id.ac.tazkia.smilemahasiswa.service.surat;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratPenelitianDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratPenelitian;
import id.ac.tazkia.smilemahasiswa.utils.FontHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.OutputStream;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.chrono.HijrahDate;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class SuratPenelitianService {

    @Autowired
    private SuratPenelitianDao suratPenelitianDao;

    @Value("${logo.surat}")
    private String logo;

    public void cetakSuratPenelitian(String id, OutputStream outputStream) throws Exception {
        log.info("Memulai download surat penelitian untuk ID: {}", id);

        SuratPenelitian suratPenelitian = suratPenelitianDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan dengan ID: " + id));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // === TANGGAL HIJRIAH ===
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en"));
        String hijriah = islamicDate.format(fmt)
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada II", "Jumadil Akhir")
                .replace("Jumada al-Akhir", "Jumadil Akhir")
                .replace("Jumada al-Awwal", "Jumadil Awal")
                .replace("Rabiʻ I", "Rabi'ul Awal")
                .replace("Rabiʻ II", "Rabi'ul Akhir")
                .replace("Rabi al-Akhir", "Rabi'ul Akhir")
                .replace("Rabi al-Awwal", "Rabi'ul Awal")
                .replace("Jumadil AwalI", "Jumadil Awal")
                .replace("Jumadil AkhirI", "Jumadil Akhir");

        // === HELPER: Load Image dengan Error Handling ===
        java.util.function.BiFunction<String, Float, Image> loadImage = (filename, scale) -> {
            try {
                String fullPath = logo + File.separator + filename;
                log.debug("Loading image: {}", fullPath);
                Image img = Image.getInstance(fullPath);
                img.scaleToFit(scale, scale);
                return img;
            } catch (Exception e) {
                log.error("Gagal load gambar: {} | Error: {}", filename, e.getMessage());
                return null;
            }
        };

        // === BACKGROUND ===
        Image background = loadImage.apply("LOGO TR.png", 400f);
        if (background != null) {
            background.setAbsolutePosition(400, 190);
            background.scaleAbsolute(400, 450);
            document.add(background);
        }

        // === BISMILLAH ===
        Image bismillah = loadImage.apply("bismillah.png", 140f);
        if (bismillah != null) {
            bismillah.setAbsolutePosition(222, PageSize.A4.getHeight() - bismillah.getScaledHeight() - 90);
            document.add(bismillah);
        }

        // === LOGO TAZKIA ===
        Image logoTazkia = loadImage.apply("Logo - UNITAZ.png", 120f);
        if (logoTazkia != null) {
            logoTazkia.setAbsolutePosition(25, PageSize.A4.getHeight() - logoTazkia.getScaledHeight() - 15);
            document.add(logoTazkia);
        }

        document.add(new Paragraph("\n\n\n\n\n"));

        Paragraph tanggalCetak = new Paragraph(formatTanggalCetak(),
                FontHelper.getSafeFont(11));
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        tanggalCetak.setIndentationLeft(50);
        tanggalCetak.setIndentationRight(50);
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(tanggalCetak);

        document.add(new Paragraph("\n"));

        // === NOMOR & HAL (SEJAJAR) ===
        String nomorSurat = generateNomorSurat(suratPenelitian);
        Font fontSurat = FontHelper.getSafeFont(11);

        PdfPTable wrapperTable = new PdfPTable(1);
        wrapperTable.setWidthPercentage(100);

        PdfPTable headerTable = new PdfPTable(new float[]{2f, 0.3f, 6f});
        headerTable.setWidthPercentage(70);
        headerTable.setSpacingBefore(5);

        PdfPCell cell;

        cell = new PdfPCell(new Phrase("Nomor", fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell(new Phrase(":", fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell(new Phrase(nomorSurat, fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell(new Phrase("Hal", fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell(new Phrase(":", fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        cell = new PdfPCell(new Phrase("Permohonan Penelitian", fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        PdfPCell wrapperCell = new PdfPCell(headerTable);
        wrapperCell.setBorder(Rectangle.NO_BORDER);
        wrapperCell.setPaddingLeft(50);
        wrapperTable.addCell(wrapperCell);

        document.add(wrapperTable);

        // === KEPADA YTH ===
        Paragraph kepada = new Paragraph();
        kepada.setAlignment(Element.ALIGN_JUSTIFIED);
        kepada.setIndentationLeft(50);
        kepada.setIndentationRight(50);

        kepada.add(new Chunk("Kepada Yth.\n", FontFactory.getFont(FontFactory.TIMES_BOLD, 11)));
        kepada.add(new Chunk(suratPenelitian.getDivisi(), FontFactory.getFont(FontFactory.TIMES_BOLD, 11)));
        document.add(kepada);

        Paragraph namaPerusahaan = new Paragraph(
                suratPenelitian.getNamaPerusahaan(),
                FontFactory.getFont(FontFactory.TIMES_BOLD, 11)
        );
        namaPerusahaan.setAlignment(Element.ALIGN_JUSTIFIED);
        namaPerusahaan.setIndentationLeft(50);
        namaPerusahaan.setIndentationRight(50);
        document.add(namaPerusahaan);

        if (suratPenelitian.getAlamat() != null && !suratPenelitian.getAlamat().isEmpty()) {
            Paragraph alamat = new Paragraph(
                    suratPenelitian.getAlamat(),
                    FontHelper.getSafeFont(11)
            );
            alamat.setAlignment(Element.ALIGN_JUSTIFIED);
            alamat.setIndentationLeft(50);
            alamat.setIndentationRight(50);
            document.add(alamat);
        }



        document.add(new Paragraph("\n"));

        // === GAMBAR SALAM ===
        Image salam = loadImage.apply("salam.png", 140f);
        if (salam != null) {
            Paragraph pSalam = new Paragraph();
            pSalam.setIndentationLeft(50);
            pSalam.add(new Chunk(salam, 0, -5, true));
            document.add(pSalam);
        }

        // === SALAM ISLAMIYAH ===
        Font fStandard = FontHelper.getSafeFont(11);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 11);

        Paragraph salamIslami = new Paragraph("السـلام عـلـيكم ورحـمة الله وبركا تــه", fStandard);
        salamIslami.setAlignment(Element.ALIGN_RIGHT);
        document.add(salamIslami);

        Paragraph perkenalan = new Paragraph();
        perkenalan.setAlignment(Element.ALIGN_JUSTIFIED);
        perkenalan.setIndentationLeft(50);
        perkenalan.setIndentationRight(50);
        perkenalan.add(new Chunk("Pertama-tama perkenankan kami menghaturkan Tahiyyah Islamiyah semoga Pimpinan dan Staf ", fStandard));
        perkenalan.add(new Chunk(suratPenelitian.getNamaPerusahaan(), fontB));
        perkenalan.add(new Chunk(" senantiasa berada dalam lindungan Allah SWT, Aamin.", fStandard));
        document.add(perkenalan);
        document.add(new Paragraph("\n", fStandard)); // Tambah spacing lebih jelas

        // === ISI SURAT ===
        String gelar = getGelarLulusan(suratPenelitian.getMahasiswa().getIdProdi());
        // Hapus titik di akhir gelar jika ada untuk menghindari line break yang tidak diinginkan
        if (gelar.endsWith(".")) {
            gelar = gelar.substring(0, gelar.length() - 1);
        }

        Paragraph isi = new Paragraph();
        isi.setAlignment(Element.ALIGN_JUSTIFIED);
        isi.setIndentationLeft(50);
        isi.setIndentationRight(50);
        isi.add(new Chunk("Selanjutnya kami sampaikan bahwa mahasiswa kami akan melaksanakan riset/skripsi sebagai salah satu tugas akademik dalam rangka memperoleh gelar ", fStandard));
        isi.add(new Chunk(gelar, fontB));
        isi.add(new Chunk(". Sehubungan dengan hal tersebut di atas, kami mohon kiranya Bapak/Ibu dapat memberikan bantuan kepada mahasiswa kami, yaitu:", fStandard));
        document.add(isi);
        document.add(new Paragraph("\n", fStandard));

        // === TABEL DATA MAHASISWA ===
        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[] { 8, 1, 21 });
        table.setWidthPercentage(80);

        addTableCell(table, "Nama", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratPenelitian.getMahasiswa().getNama(), fStandard);

        addTableCell(table, "NIM", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratPenelitian.getMahasiswa().getNim(), fStandard);

        addTableCell(table, "Program Studi", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratPenelitian.getMahasiswa().getIdProdi().getNamaProdi(), fStandard);

        addTableCell(table, "Judul Skripsi", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCellWithWrapping(table, suratPenelitian.getJudulSkripsi(), fStandard);

        addTableCell(table, "No. Telepon", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratPenelitian.getNomorTelepon(), fStandard);

        document.add(table);
        document.add(new Paragraph("\n", fStandard));

        // === PENUTUP ===
        Paragraph penutup = new Paragraph();
        penutup.setAlignment(Element.ALIGN_JUSTIFIED);
        penutup.setIndentationLeft(50);
        penutup.setIndentationRight(50);
        penutup.add(new Chunk("Demikian surat permohonan ini kami sampaikan. Atas izin dan kerjasamanya kami ucapkan terima kasih.", fStandard));
        document.add(penutup);
        document.add(new Paragraph("\n", fStandard));

        // === WASSALAM ===
        Image wassalam = loadImage.apply("wassalam.png", 140f);
        if (wassalam != null) {
            Paragraph pWassalam = new Paragraph();
            pWassalam.setIndentationLeft(50);
            pWassalam.add(new Chunk(wassalam, 0, -5, true));
            document.add(pWassalam);
        }

        // === JABATAN, BARCODE, DAN TTD DIGITAL ===
        PdfPTable signTable = new PdfPTable(1);
        signTable.setWidthPercentage(60);
        signTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingLeft(50);

        Paragraph jabatan = new Paragraph("Hormat kami,", fStandard);
        leftCell.addElement(jabatan);

        Paragraph koordinator = new Paragraph(
                "Koordinator Program Studi " + suratPenelitian.getMahasiswa().getIdProdi().getNamaProdi(), fStandard);
        leftCell.addElement(koordinator);

        try {
            String verificationUrl = "https://smile.tazkia.ac.id/surat/verifikasi/" + suratPenelitian.getId();
            BufferedImage qrImage = qrBarcode(verificationUrl, 120);
            Image qrPdfImage = Image.getInstance(qrImage, null);
            qrPdfImage.scaleAbsolute(70, 70);
            leftCell.addElement(qrPdfImage);
        } catch (Exception e) {
            log.error("Gagal buat QR Code: {}", e.getMessage());
            Paragraph fallback = new Paragraph("QR Error", FontHelper.getSafeFont(8));
            fallback.setAlignment(Element.ALIGN_CENTER);
            leftCell.addElement(fallback);
        }

        Paragraph namaKoordinator = new Paragraph(
                getNamaKoordinator(suratPenelitian.getMahasiswa()), fontB);
        namaKoordinator.setSpacingBefore(5);
        leftCell.addElement(namaKoordinator);

        Paragraph nikNidn = new Paragraph(
                getNikNidn(suratPenelitian.getMahasiswa()), fontB);
        leftCell.addElement(nikNidn);

        signTable.addCell(leftCell);
        document.add(signTable);

        // === FOOTER ===
        Image footer = loadImage.apply("Footer.jpg", 0f);
        if (footer != null) {
            float marginLeftRight = -10f;
            float marginBottom = 2f;
            float maxWidth = PageSize.A4.getWidth() - (2 * marginLeftRight);

            footer.scaleToFit(maxWidth, 200f);
            footer.setAbsolutePosition(marginLeftRight, marginBottom);
            document.add(footer);
        }

        document.close();
        log.info("PDF surat penelitian berhasil dibuat untuk ID: {}", id);
    }

    private String generateNomorSurat(SuratPenelitian suratPenelitian) {
        LocalDate today = LocalDate.now();
        String kodeProdi = getKodeProdi(suratPenelitian.getMahasiswa().getIdProdi().getNamaProdi());
        String romawiBulan = getRomawiBulan(today.getMonthValue());

        return String.format("%03d-B/Universitas-Tazkia/Prodi-%s/%s/%s",
                getUrutanSurat(today), kodeProdi, romawiBulan, today.getYear());
    }

    private Integer getUrutanSurat(LocalDate today) {
        // TODO: Implement proper sequence number generation
        return 45; // Mock number, replace with actual sequence logic
    }

    private String getRomawiBulan(int bulan) {
        String[] romawi = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
        return romawi[bulan - 1];
    }

    private String getKodeProdi(String namaProdi) {
        // Generate kode prodi dari nama prodi
        if (namaProdi.toLowerCase().contains("komunikasi")) return "KPI";
        if (namaProdi.toLowerCase().contains("akuntansi")) return "AS";
        if (namaProdi.toLowerCase().contains("manajemen")) return "MBS";
        return "PRO";
    }

    private String getGelarLulusan(Prodi prodi) {
        // Mengembalikan gelar lulusan dari field gelar di entity Prodi
        if (prodi != null && prodi.getGelar() != null && !prodi.getGelar().isEmpty()) {
            return prodi.getGelar();
        }
        // Default gelar jika field gelar kosong
        return "Sarjana Strata-1 (S1)";
    }

    private String getTanggalHijri() {
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en"));
        String hijriah = islamicDate.format(formatter)
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada II", "Jumadil Akhir")
                .replace("Jumada al-Akhir", "Jumadil Akhir")
                .replace("Jumada al-Awwal", "Jumadil Awal")
                .replace("Rabiʻ I", "Rabi'ul Awal")
                .replace("Rabiʻ II", "Rabi'ul Akhir")
                .replace("Rabi al-Akhir", "Rabi'ul Akhir")
                .replace("Rabi al-Awwal", "Rabi'ul Awal")
                .replace("Jumadil AwalI", "Jumadil Awal")
                .replace("Jumadil AkhirI", "Jumadil Akhir");
        return hijriah + " H.";
    }

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
        return date.format(formatter);
    }

    private String formatTanggalCetak() {
        LocalDate today = LocalDate.now();
        String tanggalMasehi = formatDate(today);
        String tanggalHijri = getTanggalHijri();
        return tanggalMasehi + " / " + tanggalHijri;
    }

    private String getNamaKoordinator(Mahasiswa mahasiswa) {
        try {
            if (mahasiswa.getIdProdi() != null &&
                mahasiswa.getIdProdi().getDosen() != null &&
                mahasiswa.getIdProdi().getDosen().getKaryawan() != null) {
                return mahasiswa.getIdProdi().getDosen().getKaryawan().getNamaKaryawan();
            }
        } catch (Exception e) {
            log.error("Error getting koordinator name for prodi {}: {}",
                mahasiswa.getIdProdi().getNamaProdi(), e.getMessage());
        }
        return "Koordinator Program Studi " + mahasiswa.getIdProdi().getNamaProdi();
    }

    private String getNikNidn(Mahasiswa mahasiswa) {
        try {
            if (mahasiswa.getIdProdi() != null &&
                mahasiswa.getIdProdi().getDosen() != null &&
                mahasiswa.getIdProdi().getDosen().getKaryawan() != null) {
                String nik = mahasiswa.getIdProdi().getDosen().getKaryawan().getNik();
                String nidn = mahasiswa.getIdProdi().getDosen().getKaryawan().getNidn();

                if (nik != null && nidn != null) {
                    return "NIK./NIDN.: " + nik + "/" + nidn;
                } else if (nidn != null) {
                    return "NIDN.: " + nidn;
                } else if (nik != null) {
                    return "NIK.: " + nik;
                }
            }
        } catch (Exception e) {
            log.error("Error getting NIK/NIDN for koordinator prodi {}: {}",
                mahasiswa.getIdProdi().getNamaProdi(), e.getMessage());
        }
        return "NIK./NIDN.: -";
    }

    // === HELPER TABEL ===
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    // Helper untuk cell dengan wrapping (khusus untuk judul skripsi panjang)
    private void addTableCellWithWrapping(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPaddingTop(3f);
        cell.setPaddingBottom(3f);
        cell.setLeading(12f, 0f); // Line spacing untuk judul yang panjang
        table.addCell(cell);
    }

    // === HELPER QR CODE ===
    private static BufferedImage qrBarcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // Get surat data untuk HTML template (backward compatibility)
    public Map<String, Object> prepareSuratPenelitianData(String id) {
        SuratPenelitian suratPenelitian = suratPenelitianDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan dengan ID: " + id));

        Map<String, Object> data = new HashMap<>();
        data.put("surat", suratPenelitian);
        data.put("nomorSurat", generateNomorSurat(suratPenelitian));
        data.put("halaman", "Permohonan Penelitian");
        data.put("tanggalHijri", getTanggalHijri());
        data.put("tanggalCetak", formatTanggalCetak());
        data.put("barcodeUrl", generateBarcodeUrl(generateNomorSurat(suratPenelitian)));

        return data;
    }

    private String generateBarcodeUrl(String text) {
        try {
            String encodedText = java.net.URLEncoder.encode(text, "UTF-8");
            return "https://barcode.tec-it.com/barcode-service.ashx?code=" + encodedText + "&barcodeType=Code128&width=300&height=50";
        } catch (Exception e) {
            return "/barcode/generate?text=" + text.replace("/", "_");
        }
    }
}
