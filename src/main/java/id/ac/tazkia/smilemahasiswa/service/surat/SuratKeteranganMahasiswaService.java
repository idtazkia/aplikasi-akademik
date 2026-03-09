package id.ac.tazkia.smilemahasiswa.service.surat;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import id.ac.tazkia.smilemahasiswa.dao.surat.SuratKeteranganMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.surat.SuratKeteranganMahasiswa;
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
public class SuratKeteranganMahasiswaService {

    @Autowired
    private SuratKeteranganMahasiswaDao suratKeteranganMahasiswaDao;

    @Value("${logo.surat}")
    private String logo;

    public void cetakSuratKeteranganMahasiswa(String id, OutputStream outputStream) throws Exception {
        log.info("Memulai download surat keterangan mahasiswa untuk ID: {}", id);

        SuratKeteranganMahasiswa surat = suratKeteranganMahasiswaDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Surat tidak ditemukan dengan ID: " + id));

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, outputStream);
        document.open();

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
            float centerX = (PageSize.A4.getWidth() - 400) / 2;
            background.setAbsolutePosition(centerX, 190);
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
        

        String namaSurat = "SURAT KETERANGAN MAHASISWA";
        Paragraph judulSurat = new Paragraph(namaSurat, FontFactory.getFont(FontFactory.TIMES_BOLD, 11));
        judulSurat.setAlignment(Element.ALIGN_CENTER);
        document.add(judulSurat);

        String nomorSurat = generateNomorSurat(surat);
        Paragraph nomorSuratParagraph = new Paragraph("Nomor: " + nomorSurat, FontFactory.getFont(FontFactory.TIMES_ROMAN, 11));
        nomorSuratParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(nomorSuratParagraph);

        

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

        document.add(new Paragraph("\n"));

        // === ISI SURAT ===
        Paragraph isi1 = new Paragraph();
        isi1.setAlignment(Element.ALIGN_JUSTIFIED);
        isi1.setIndentationLeft(50);
        isi1.setIndentationRight(50);
        isi1.add(new Chunk("Pimpinan Akademik Universitas Islam Tazkia dengan ini menerangkan bahwa :", fStandard));
        document.add(isi1);

        document.add(new Paragraph("\n"));

        // === TABEL DATA MAHASISWA ===
        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[] { 10, 1, 15 });
        table.setWidthPercentage(80);

        Mahasiswa mhs = surat.getMahasiswa();

        addTableCell(table, "Nama", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, mhs.getNama(), fStandard);

        addTableCell(table, "NIM", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, mhs.getNim(), fStandard);

        String ttl = "";
        if (mhs.getTempatLahir() != null && mhs.getTanggalLahir() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
            ttl = mhs.getTempatLahir() + ", " + mhs.getTanggalLahir().format(formatter);
        }
        addTableCell(table, "Tempat, Tgl Lahir", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, ttl, fStandard);

        String alamat = buildAlamat(mhs);
        addTableCell(table, "Alamat", fStandard);
        addTableCell(table, ":", fStandard);
        addTableCell(table, alamat, fStandard);

        document.add(table);

        document.add(new Paragraph("\n"));

        // === KETERANGAN AKTIF ===
        String semesterAktif = getSemesterAktif(mhs);

        Paragraph isi2 = new Paragraph();
        isi2.setAlignment(Element.ALIGN_JUSTIFIED);
        isi2.setIndentationLeft(50);
        isi2.setIndentationRight(50);
        isi2.add(new Chunk("benar terdaftar sebagai mahasiswa aktif ", fStandard));
        isi2.add(new Chunk(semesterAktif, fontB));
        isi2.add(new Chunk(" pada Program Studi ", fStandard));
        isi2.add(new Chunk(mhs.getIdProdi().getNamaProdi(), fontB));
        isi2.add(new Chunk(" Fakultas Ekonomi dan Bisnis Syariah.", fStandard));
        document.add(isi2);

        document.add(new Paragraph("\n"));

        // === PENUTUP ===
        Paragraph penutup = new Paragraph();
        penutup.setAlignment(Element.ALIGN_JUSTIFIED);
        penutup.setIndentationLeft(50);
        penutup.setIndentationRight(50);
        penutup.add(new Chunk("Demikian surat keterangan ini dibuat untuk dipergunakan sebagaimana mestinya.", fStandard));
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

        document.add(new Paragraph("\n"));

        // === JABATAN, QR CODE, DAN TTD ===
        PdfPTable signTable = new PdfPTable(1);
        signTable.setWidthPercentage(60);
        signTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setPaddingLeft(50);

        // Tanggal surat dikeluarkan
        String tanggalTtd = getTanggalTtd();
        Paragraph tanggalParagraph = new Paragraph(tanggalTtd, fStandard);
        tanggalParagraph.setAlignment(Element.ALIGN_LEFT);
        tanggalParagraph.setIndentationLeft(50);
        tanggalParagraph.setSpacingAfter(5);

        document.add(tanggalParagraph);

        Paragraph jabatan = new Paragraph("Kepala Bagian Akademik,", fStandard);
        rightCell.addElement(jabatan);

        try {
            String verificationUrl = "https://tazkia.ac.id/surat/verifikasi/" + surat.getId();
            BufferedImage qrImage = qrBarcode(verificationUrl, 120);
            Image qrPdfImage = Image.getInstance(qrImage, null);
            qrPdfImage.scaleAbsolute(70, 70);
            qrPdfImage.setSpacingBefore(10f);
            rightCell.addElement(qrPdfImage);
        } catch (Exception e) {
            log.error("Gagal buat QR Code: {}", e.getMessage());
            Paragraph fallback = new Paragraph("QR Error", FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
            fallback.setAlignment(Element.ALIGN_CENTER);
            rightCell.addElement(fallback);
        }

        // Get nama dan NIK penandatangan dari userApproval
        String namaPenandatanganStr = "Mohammad Bintang Pamuncak, S.E.I., M.Sc, Ph.D";
        String nikNidnStr = "NIK: 2120089305 / NIDN: 015.173";

        if (surat.getUserApproval() != null) {
            namaPenandatanganStr = surat.getUserApproval().getNamaKaryawan();
            StringBuilder nikNidnBuilder = new StringBuilder();

            if (surat.getUserApproval().getNik() != null) {
                nikNidnBuilder.append("NIK: ").append(surat.getUserApproval().getNik());
            }
            if (surat.getUserApproval().getNidn() != null) {
                if (nikNidnBuilder.length() > 0) {
                    nikNidnBuilder.append(" / ");
                }
                nikNidnBuilder.append("NIDN: ").append(surat.getUserApproval().getNidn());
            }

            if (nikNidnBuilder.length() > 0) {
                nikNidnStr = nikNidnBuilder.toString();
            }
        }

        Paragraph namaPenandatangan = new Paragraph(namaPenandatanganStr, FontFactory.getFont(FontFactory.TIMES_BOLD, 11, Font.UNDERLINE));
        namaPenandatangan.setSpacingBefore(5);
        rightCell.addElement(namaPenandatangan);

        Paragraph nikNidn = new Paragraph(nikNidnStr, fontB);
        rightCell.addElement(nikNidn);

        signTable.addCell(rightCell);
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
        log.info("PDF surat keterangan mahasiswa berhasil dibuat untuk ID: {}", id);
    }

    private String generateNomorSurat(SuratKeteranganMahasiswa surat) {
        LocalDate today = LocalDate.now();

        return String.format("430-B/Ket/Universitas-Tazkia/Akd/%d/%s",
                today.getMonthValue(), today.getYear());
    }

    private String getTanggalTtd() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatterMasehi = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id"));
        String tanggalMasehi = today.format(formatterMasehi);

        HijrahDate islamicDate = HijrahDate.from(today);
        DateTimeFormatter formatterHijri = DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("en"));
        String hijriah = islamicDate.format(formatterHijri)
                .replace("Jumada I", "Jumadil Awal")
                .replace("Jumada II", "Jumadil Akhir")
                .replace("Jumada al-Akhir", "Jumadil Akhir")
                .replace("Jumada al-Awwal", "Jumadil Awal")
                .replace("Rabi' I", "Rabi'ul Awal")
                .replace("Rabi' II", "Rabi'ul Akhir")
                .replace("Rabi al-Akhir", "Rabi'ul Akhir")
                .replace("Rabi al-Awwal", "Rabi'ul Awal");

        return tanggalMasehi + " M/ " + hijriah + " H";
    }

    private String getSemesterAktif(Mahasiswa mahasiswa) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int year = now.getYear();

        String semester;
        if (month >= 8 && month <= 12) {
            semester = "Ganjil";
        } else if (month >= 1 && month <= 6) {
            semester = "Genap";
        } else {
            semester = "Ganjil";
        }

        String tahunAkademik;
        if (month >= 8 && month <= 12) {
            tahunAkademik = year + "/" + (year + 1);
        } else {
            tahunAkademik = (year - 1) + "/" + year;
        }

        return "Semester " + semester + " Tahun Akademik " + tahunAkademik;
    }

    private String buildAlamat(Mahasiswa mhs) {
        StringBuilder alamat = new StringBuilder();

        if (mhs.getNamaJalan() != null && !mhs.getNamaJalan().isEmpty()) {
            alamat.append(mhs.getNamaJalan());
        }

        if (mhs.getRt() != null && !mhs.getRt().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append("RT ").append(mhs.getRt());
        }

        if (mhs.getRw() != null && !mhs.getRw().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append("RW ").append(mhs.getRw());
        }

        if (mhs.getNamaDusun() != null && !mhs.getNamaDusun().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append(mhs.getNamaDusun());
        }

        if (mhs.getIdKelurahan() != null && !mhs.getIdKelurahan().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append(mhs.getIdKelurahan());
        }

        if (mhs.getIdKecamatan() != null && !mhs.getIdKecamatan().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", Kec. ");
            alamat.append(mhs.getIdKecamatan());
        }

        if (mhs.getIdKotaKabupaten() != null && !mhs.getIdKotaKabupaten().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append(mhs.getIdKotaKabupaten());
        }

        if (mhs.getIdProvinsi() != null && !mhs.getIdProvinsi().isEmpty()) {
            if (alamat.length() > 0) alamat.append(", ");
            alamat.append(mhs.getIdProvinsi());
        }

        return alamat.length() > 0 ? alamat.toString() : "-";
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
}
