package id.ac.tazkia.smilemahasiswa.service.surat;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratTugasKuliahDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratTugasKuliah;
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
public class SuratTugasKuliahService {

    @Autowired
    private SuratTugasKuliahDao suratTugasKuliahDao;

    @Value("${logo.surat}")
    private String logo;

    public void cetakSuratTugasKuliah(String id, OutputStream outputStream) throws Exception {
        log.info("Memulai download surat tugas kuliah untuk ID: {}", id);

        SuratTugasKuliah surat = suratTugasKuliahDao.findById(id)
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

        // === TANGGAL CETAK ===
        Paragraph tanggalCetak = new Paragraph(formatTanggalCetak(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 11));
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        tanggalCetak.setIndentationLeft(50);
        tanggalCetak.setIndentationRight(50);
        tanggalCetak.setAlignment(Element.ALIGN_JUSTIFIED);
        document.add(tanggalCetak);

        document.add(new Paragraph("\n"));

        // === NOMOR & HAL (SEJAJAR) ===
        String nomorSurat = generateNomorSurat(surat);
        Font fontSurat = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);

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

        cell = new PdfPCell(new Phrase(surat.getHal(), fontSurat));
        cell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(cell);

        PdfPCell wrapperCell = new PdfPCell(headerTable);
        wrapperCell.setBorder(Rectangle.NO_BORDER);
        wrapperCell.setPaddingLeft(50);
        wrapperTable.addCell(wrapperCell);

        document.add(wrapperTable);

        Paragraph kepada = new Paragraph(
                "Kepada Yth.\nPimpinan " + surat.getNamaPerusahaan(),
                FontFactory.getFont(FontFactory.TIMES_BOLD, 11));
        kepada.setAlignment(Element.ALIGN_JUSTIFIED);
        kepada.setIndentationLeft(50);
        kepada.setIndentationRight(50);
        document.add(kepada);

        Paragraph alamat = new Paragraph(
                surat.getAlamatPerusahaan(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 11)
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

        // === SALAM ISLAMIYAH ===
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 11);

        Paragraph salamIslami = new Paragraph("السـلام عـلـيكم ورحـمة الله وبركا تــه", fStandard);
        salamIslami.setAlignment(Element.ALIGN_RIGHT);
        document.add(salamIslami);

        Paragraph perkenalan = new Paragraph();
        perkenalan.setAlignment(Element.ALIGN_JUSTIFIED);
        perkenalan.setIndentationLeft(50);
        perkenalan.setIndentationRight(50);
        perkenalan.add(new Chunk("Pertama-tama perkenankan kami menghaturkan Tahiyyah Islamiyyah semoga segenap pimpinan dan staf ", fStandard));
        perkenalan.add(new Chunk(surat.getNamaPerusahaan(), fontB));
        perkenalan.add(new Chunk(" senantiasa sehat wal afiat serta sukses dalam menjalankan aktifitas sehari-hari. Amin.", fStandard));
        document.add(perkenalan);
        document.add(new Paragraph("\n"));

        // === ISI SURAT ===
        Paragraph isi = new Paragraph();
        isi.setAlignment(Element.ALIGN_JUSTIFIED);
        isi.setIndentationLeft(50);
        isi.setIndentationRight(50);

        String namaMatakuliah = surat.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah();

        isi.add(new Chunk("Sehubungan dengan adanya tugas perkuliahan mini riset pada mata kuliah ", fStandard));
        isi.add(new Chunk(namaMatakuliah, fontB));
        isi.add(new Chunk(", maka untuk mahasiswa berikut :", fStandard));

        document.add(isi);
        document.add(new Paragraph("\n"));

        // === TABEL DATA MAHASISWA ===
        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[] { 10, 1, 15 });
        table.setWidthPercentage(80);

        addTableCell(table, "Nama", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, surat.getMahasiswa().getNama(), fStandard);

        addTableCell(table, "NIM", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, surat.getMahasiswa().getNim(), fStandard);

        addTableCell(table, "Program Studi", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, surat.getMahasiswa().getIdProdi().getNamaProdi(), fStandard);

        addTableCell(table, "No Handphone", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, getNoTelepon(surat.getMahasiswa()), fStandard);

        document.add(table);
        document.add(new Paragraph("\n"));

        // === PENUTUP ===
        Paragraph penutup = new Paragraph();
        penutup.setAlignment(Element.ALIGN_JUSTIFIED);
        penutup.setIndentationLeft(50);
        penutup.setIndentationRight(50);
        penutup.add(new Chunk("dimohonkan untuk dapat melaksanakan wawancara penelitian di ", fStandard));
        penutup.add(new Chunk(surat.getNamaPerusahaan(), fontB));
        penutup.add(new Chunk(" guna penyelesaian tugas kuliah.", fStandard));
        document.add(penutup);
        document.add(new Paragraph("\n"));

        Paragraph penutup2 = new Paragraph(
                "Demikian surat permohonan ini dibuat dan besar harapan kami untuk dapat disetujui. Terima kasih.", fStandard);
        penutup2.setAlignment(Element.ALIGN_JUSTIFIED);
        penutup2.setIndentationLeft(50);
        penutup2.setIndentationRight(50);
        document.add(penutup2);
        document.add(new Paragraph("\n"));

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
                "Koordinator Program Studi " + surat.getMahasiswa().getIdProdi().getNamaProdi(), fStandard);
        leftCell.addElement(koordinator);

        try {
            String verificationUrl = "https://tazkia.ac.id/surat/verifikasi/" + surat.getId();
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
                getNamaKoordinator(surat.getMahasiswa()), fontB);
        namaKoordinator.setSpacingBefore(5);
        leftCell.addElement(namaKoordinator);

        Paragraph nikNidn = new Paragraph(
                getNikNidn(surat.getMahasiswa()), fontB);
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
        log.info("PDF surat tugas kuliah berhasil dibuat untuk ID: {}", id);
    }

    private String generateNomorSurat(SuratTugasKuliah surat) {
        LocalDate today = LocalDate.now();
        String kodeProdi = getKodeProdi(surat.getMahasiswa().getIdProdi().getNamaProdi());
        String romawiBulan = getRomawiBulan(today.getMonthValue());

        return String.format("%03d-B/Universitas-Tazkia/Prodi-%s/%s/%s",
                getUrutanSurat(today), kodeProdi, romawiBulan, today.getYear());
    }

    private Integer getUrutanSurat(LocalDate today) {
        // TODO: Implement proper sequence number generation
        return 1; // Mock number, replace with actual sequence logic
    }

    private String getRomawiBulan(int bulan) {
        String[] romawi = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII"};
        return romawi[bulan - 1];
    }

    private String getKodeProdi(String namaProdi) {
        if (namaProdi.toLowerCase().contains("komunikasi")) return "KPI";
        if (namaProdi.toLowerCase().contains("akuntansi")) return "AS";
        if (namaProdi.toLowerCase().contains("manajemen")) return "MBS";
        return "PRO";
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

    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static BufferedImage qrBarcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H);
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }
}
