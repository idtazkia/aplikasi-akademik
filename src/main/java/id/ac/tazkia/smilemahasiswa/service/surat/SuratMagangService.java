package id.ac.tazkia.smilemahasiswa.service.surat;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratMagangDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratMagang;
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
public class SuratMagangService {

    @Autowired
    private SuratMagangDao suratMagangDao;

    @Value("${logo.surat}")
    private String logo;

    public void cetakSuratMagang(String id, OutputStream outputStream) throws Exception {
        log.info("Memulai download surat magang untuk ID: {}", id);

        SuratMagang suratMagang = suratMagangDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan dengan ID: " + id));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

        // === TANGGAL HIJRIAH ===
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en"));
        String hijriah = islamicDate.format(fmt)
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada II", "Jumadil Akhir")
                .replace("Rabiʻ I", "Rabi'ul Awal")
                .replace("Rabiʻ II", "Rabi'ul Akhir");

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

        document.add(new Paragraph("\n\n\n\n\n\n"));

       

        Paragraph tanggalCetak = new Paragraph(formatTanggalCetak(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 10));
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        tanggalCetak.setIndentationLeft(50);
        tanggalCetak.setIndentationRight(50);
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(tanggalCetak);

        document.add(new Paragraph("\n"));

        // === NOMOR SURAT ===
        String nomorSurat = generateNomorSurat(suratMagang);
        Paragraph nomorSuratParagraph = new Paragraph(
                "Nomor: " + nomorSurat,
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 10));
        nomorSuratParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        nomorSuratParagraph.setIndentationLeft(50);
        nomorSuratParagraph.setIndentationRight(50);
        document.add(nomorSuratParagraph);

        Paragraph hal = new Paragraph(
                "Hal: Permohonan Magang",
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 10));
        hal.setAlignment(Element.ALIGN_JUSTIFIED);
        hal.setIndentationLeft(50);
        hal.setIndentationRight(50);
        document.add(hal);

        Paragraph kepada = new Paragraph(
                "Kepada Yth.\nPimpinan " + suratMagang.getNamaPerusahaan(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 10));
        kepada.setAlignment(Element.ALIGN_JUSTIFIED);
        kepada.setIndentationLeft(50);
        kepada.setIndentationRight(50);
        document.add(kepada);

        Paragraph alamat = new Paragraph(
                suratMagang.getAlamat(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 10)
        );
        alamat.setAlignment(Element.ALIGN_JUSTIFIED);
        alamat.setIndentationLeft(50);
        alamat.setIndentationRight(50);
        document.add(alamat);

        document.add(new Paragraph("\n"));


        // === GAMBAR SALAM ===
        Image salam = loadImage.apply("salam.png", 140f);
        if (salam != null) {
            Paragraph pSalam = new Paragraph();
            pSalam.setIndentationLeft(50);
            pSalam.add(new Chunk(salam, 0, -5, true));
            document.add(pSalam);
        }
        document.add(new Paragraph("\n"));

        // === SALAM ISLAMIYAH ===
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 10);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 10);

        Paragraph salamIslami = new Paragraph("السـلام عـلـيكم ورحـمة الله وبركا تــه", fStandard);
        salamIslami.setAlignment(Element.ALIGN_RIGHT);
        document.add(salamIslami);

        Paragraph perkenalan = new Paragraph("Pertama-tama perkenankan kami menghaturkan Tahiyyah Islamiyah semoga Pimpinan dan Staf "
                + suratMagang.getNamaPerusahaan() + " senantiasa berada dalam lindungan Allah SWT, Amin.", fStandard);
        perkenalan.setAlignment(Element.ALIGN_JUSTIFIED);
        perkenalan.setIndentationLeft(50);
        perkenalan.setIndentationRight(50);
        perkenalan.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(perkenalan);
        document.add(new Paragraph("\n"));

        // === ISI SURAT ===
        Paragraph isi = new Paragraph();
        isi.setAlignment(Element.ALIGN_JUSTIFIED);
        isi.setIndentationLeft(50);
        isi.setIndentationRight(50);
        isi.add(new Chunk("Sehubungan dengan program pelaksanaan studi melalui program MBKM (Merdeka Belajar Kampus Merdeka), maka dengan ini kami mengajukan permohonan kepada Bapak/Ibu untuk dapat kiranya memberikan kesempatan magang kepada mahasiswa kami terhitung dari tanggal ", fStandard));
        isi.add(new Chunk(formatDate(suratMagang.getTanggalMulaiMagang()), fontB));
        isi.add(new Chunk(" s.d ", fStandard));
        isi.add(new Chunk(formatDate(suratMagang.getTanggalSelesaiMagang()), fontB));
        isi.add(new Chunk(" atau selama ", fStandard));
        isi.add(new Chunk(String.valueOf(calculateHari(suratMagang.getTanggalMulaiMagang(), suratMagang.getTanggalSelesaiMagang())), fontB));
        isi.add(new Chunk(" (delapan puluh) hari kerja. Adapun Mahasiswanya yaitu:", fStandard));
        document.add(isi);
        document.add(new Paragraph("\n"));

        // === TABEL DATA MAHASISWA ===
        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[] { 10, 1, 15 });
        table.setWidthPercentage(80);

        addTableCell(table, "Nama", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratMagang.getMahasiswa().getNama(), fStandard);

        addTableCell(table, "NIM", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratMagang.getMahasiswa().getNim(), fStandard);

        addTableCell(table, "Program Studi", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, suratMagang.getMahasiswa().getIdProdi().getNamaProdi(), fStandard);

        addTableCell(table, "No. Telepon", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, getNoTelepon(suratMagang.getMahasiswa()), fStandard);

        document.add(table);
        document.add(new Paragraph("\n"));

        // === PENUTUP ===
        Paragraph penutup = new Paragraph(
                "Demikian surat permohonan ini kami sampaikan, atas kesediaan dan kerjasamanya kami ucapkan terima kasih.", fStandard);
        penutup.setAlignment(Element.ALIGN_JUSTIFIED);
        penutup.setIndentationLeft(50);
        penutup.setIndentationRight(50);
        document.add(penutup);
        document.add(new Paragraph("\n"));

        // === WASSALAM ===
        Image wassalam = loadImage.apply("wassalam.png", 140f);
        if (wassalam != null) {
            Paragraph pWassalam = new Paragraph();
            pWassalam.setIndentationLeft(50);
            pWassalam.add(new Chunk(wassalam, 0, -5, true));
            document.add(pWassalam);
        }

        // === TANGGAL ===
        Paragraph tanggal = new Paragraph();
        tanggal.setIndentationLeft(50);
        tanggal.add(new Chunk("Bogor, ", fStandard));
        tanggal.add(new Chunk(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) + " M", fStandard));
        tanggal.add(new Chunk(" / ", fStandard));
        tanggal.add(new Chunk(hijriah + " H", fStandard));
        document.add(tanggal);

        // === JABATAN, BARCODE, DAN TTD DIGITAL ===
        PdfPTable signTable = new PdfPTable(1);
        signTable.setWidthPercentage(60);
        signTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setPaddingLeft(50);

        Paragraph jabatan = new Paragraph("Hormat kami,", fontB);
        jabatan.setSpacingAfter(10);
        leftCell.addElement(jabatan);

        Paragraph koordinator = new Paragraph(
                "Koordinator Program Studi " + suratMagang.getMahasiswa().getIdProdi().getNamaProdi(), fontB);
        koordinator.setSpacingBefore(10);
        leftCell.addElement(koordinator);

        try {
            String verificationUrl = "https://tazkia.ac.id/surat/verifikasi/" + suratMagang.getId();
            BufferedImage qrImage = qrBarcode(verificationUrl, 120);
            Image qrPdfImage = Image.getInstance(qrImage, null);
            qrPdfImage.scaleAbsolute(70, 70);
            leftCell.addElement(qrPdfImage);
        } catch (Exception e) {
            log.error("Gagal buat QR Code: {}", e.getMessage());
            Paragraph fallback = new Paragraph("QR Error", FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
            fallback.setAlignment(Element.ALIGN_CENTER);
            leftCell.addElement(fallback);
        }

        Paragraph namaKoordinator = new Paragraph(
                getNamaKoordinator(suratMagang.getMahasiswa()), fontB);
        namaKoordinator.setSpacingBefore(5);
        leftCell.addElement(namaKoordinator);

        Paragraph nikNidn = new Paragraph(
                getNikNidn(suratMagang.getMahasiswa()), fontB);
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
        log.info("PDF surat magang berhasil dibuat untuk ID: {}", id);
    }

    private String generateNomorSurat(SuratMagang suratMagang) {
        LocalDate today = LocalDate.now();
        String kodeProdi = getKodeProdi(suratMagang.getMahasiswa().getIdProdi().getNamaProdi());

        return String.format("%03d-B/Universitas-Tazkia/Prodi-%s/%s",
                getUrutanSurat(today), kodeProdi, today.getYear());
    }

    private Integer getUrutanSurat(LocalDate today) {
        // TODO: Implement proper sequence number generation
        return 44; // Mock number, replace with actual sequence logic
    }

    
    private String getKodeProdi(String namaProdi) {
        // Generate kode prodi dari nama prodi
        if (namaProdi.toLowerCase().contains("komunikasi")) return "KPI";
        if (namaProdi.toLowerCase().contains("sistem informasi")) return "SI";
        if (namaProdi.toLowerCase().contains("manajemen")) return "MAN";
        return "PRO";
    }

    private String getTanggalHijri() {
        HijrahDate islamicDate = HijrahDate.from(LocalDate.now());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en"));
        String hijriah = islamicDate.format(formatter)
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada II", "Jumadil Akhir")
                .replace("Rabiʻ I", "Rabi'ul Awal")
                .replace("Rabiʻ II", "Rabi'ul Akhir");
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

    private Integer calculateHari(LocalDate mulai, LocalDate selesai) {
        if (mulai == null || selesai == null) return 0;
        return (int) java.time.temporal.ChronoUnit.DAYS.between(mulai, selesai) + 1;
    }

    private String getNoTelepon(Mahasiswa mahasiswa) {
        if (mahasiswa.getTeleponSeluler() != null && !mahasiswa.getTeleponSeluler().isEmpty()) {
            return mahasiswa.getTeleponSeluler();
        }
        if (mahasiswa.getTeleponRumah() != null && !mahasiswa.getTeleponRumah().isEmpty()) {
            return mahasiswa.getTeleponRumah();
        }
        return "-";
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
                // Combine NIK and NIDN if available from Karyawan entity
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

    // === HELPER QR CODE ===
    private static BufferedImage qrBarcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    // Get surat data untuk HTML template (backward compatibility)
    public Map<String, Object> prepareSuratMagangData(String id) {
        SuratMagang suratMagang = suratMagangDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan dengan ID: " + id));

        Map<String, Object> data = new HashMap<>();
        data.put("surat", suratMagang);
        data.put("nomorSurat", generateNomorSurat(suratMagang));
        data.put("halaman", "Permohonan Magang");
        data.put("tanggalHijri", getTanggalHijri());
        data.put("tanggalCetak", formatTanggalCetak());
        data.put("barcodeUrl", generateBarcodeUrl(generateNomorSurat(suratMagang)));

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