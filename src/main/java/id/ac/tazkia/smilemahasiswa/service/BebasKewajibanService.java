package id.ac.tazkia.smilemahasiswa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.lowagie.text.*;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.chrono.HijrahDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

@Service
@Transactional
@Slf4j
public class BebasKewajibanService {

    public static final String jenjangS1 = "S1";
    public static final String jenjangS2 = "S2";

    @Value("${logo.surat}")
    private String logo;

    @Autowired
    BebasKewajibanDao bebasKewajibanDao;

    @Autowired
    KewajibanPerpusDao kewajibanPerpusDao;

    @Autowired
    KewajibanPerpusMahasiswaDao kewajibanPerpusMahasiswaDao;

    @Autowired
    HistoriBebasKewajibanDao historiBebasKewajibanDao;

    @Autowired
    KaryawanDao karyawanDao;

    @Autowired
    UserDao userDao;

    @Autowired
    TranscriptService transcriptService;

    @Autowired
    MailService mailService;

    public void saveDaftarKewajiban(Mahasiswa mahasiswa) {

        PendaftaranBebasKewajiban cek = bebasKewajibanDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
        if (cek == null) {
            PendaftaranBebasKewajiban kewajiban = new PendaftaranBebasKewajiban();
            if (jenjangS1.equalsIgnoreCase(mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang())) {
                kewajiban.setMahasiswa(mahasiswa);
            } else if (jenjangS2.equalsIgnoreCase(mahasiswa.getIdProdi().getIdJenjang().getKodeJenjang())) {
                kewajiban.setStatusApproveTlc(StatusApprove.APPROVED);
                kewajiban.setMahasiswa(mahasiswa);
            }
            bebasKewajibanDao.save(kewajiban);

            List<KewajibanPerpus> listKewajiban = kewajibanPerpusDao.findByStatusOrderById(StatusRecord.AKTIF);
            for (KewajibanPerpus list : listKewajiban) {
                KewajibanPerpusMahasiswa perpusMahasiswa = new KewajibanPerpusMahasiswa();
                perpusMahasiswa.setKewajibanPerpus(list);
                perpusMahasiswa.setBebasKewajiban(kewajiban);
                kewajibanPerpusMahasiswaDao.save(perpusMahasiswa);
            }
            // kirim notifikasi ke admin masing - masing
            kirimEmailAdmin(kewajiban);
            createFirstHistoriApproval(kewajiban);
        }
    }

    public void kirimEmailAdmin(PendaftaranBebasKewajiban bebasKewajiban) {

        // Admin perpus
        List<User> perpus = userDao.findByRoleId("perpustakaan");
        for (User u : perpus) {
            mailService.templateNotifAdmin(bebasKewajiban, "perpus", u.getUsername());
        }
        List<User> kabagperpus = userDao.findByRoleId("kabagperpus");
        for (User u : kabagperpus) {
            mailService.templateNotifAdmin(bebasKewajiban, "perpus", u.getUsername());
        }

        // Admin keuangan
        mailService.templateNotifAdmin(bebasKewajiban, "keuangan", "ahmadrisan@tazkia.ac.id");
        mailService.templateNotifAdmin(bebasKewajiban, "keuangan", "mfurqonh@tazkia.ac.id");

        // Admin TLC
        List<User> stafTlc = userDao.findByRoleId("staftlc");
        for (User u : stafTlc) {
            mailService.templateNotifAdmin(bebasKewajiban, "tlc", u.getUsername());
        }

        // Admin akademik
        mailService.templateNotifAdmin(bebasKewajiban, "akademik", "riska@tazkia.ac.id");
        mailService.templateNotifAdmin(bebasKewajiban, "akademik", "anaminuri@tazkia.ac.id");
    }

    public void createFirstHistoriApproval(PendaftaranBebasKewajiban bebasKewajiban) {
        // bagian staff perpus
        HistoriBebasKewajiban historiAwalPerpus = new HistoriBebasKewajiban();
        historiAwalPerpus.setBebasKewajiban(bebasKewajiban);
        historiAwalPerpus.setKomentar("Menunggu respon dari bagian staf perpus.");
        historiAwalPerpus.setByBagian(ByBagian.PERPUSTAKAAN);
        historiAwalPerpus.setStatusApprove(bebasKewajiban.getStatusApprovePerpus());
        historiAwalPerpus.setUrutan(1);
        historiBebasKewajibanDao.save(historiAwalPerpus);
        // bagian Kabag perpus
        HistoriBebasKewajiban historiAwalKabagPerpus = new HistoriBebasKewajiban();
        historiAwalKabagPerpus.setBebasKewajiban(bebasKewajiban);
        historiAwalKabagPerpus.setKomentar("Menunggu respon dari kepala bagian perpus.");
        historiAwalKabagPerpus.setByBagian(ByBagian.KABAG_PERPUS);
        historiAwalKabagPerpus.setStatusApprove(bebasKewajiban.getStatusApproveKabagPerpus());
        historiAwalKabagPerpus.setUrutan(2);
        historiBebasKewajibanDao.save(historiAwalKabagPerpus);
        // bagian keuangan
        HistoriBebasKewajiban historiAwalKeuangan = new HistoriBebasKewajiban();
        historiAwalKeuangan.setBebasKewajiban(bebasKewajiban);
        historiAwalKeuangan.setKomentar("Menunggu respon dari bagian keuangan.");
        historiAwalKeuangan.setByBagian(ByBagian.KEUANGAN);
        historiAwalKeuangan.setStatusApprove(bebasKewajiban.getStatusApproveKeuangan());
        historiAwalKeuangan.setUrutan(3);
        historiBebasKewajibanDao.save(historiAwalKeuangan);
        // bagian staff TLC
        HistoriBebasKewajiban historiAwalTlc = new HistoriBebasKewajiban();
        historiAwalTlc.setBebasKewajiban(bebasKewajiban);
        historiAwalTlc.setKomentar("Menunggu respon dari bagian TLC.");
        historiAwalTlc.setByBagian(ByBagian.TLC);
        historiAwalTlc.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiAwalTlc.setUrutan(4);
        historiBebasKewajibanDao.save(historiAwalTlc);
        // bagian staff Akademik
        HistoriBebasKewajiban historiAwalAkademik = new HistoriBebasKewajiban();
        historiAwalAkademik.setBebasKewajiban(bebasKewajiban);
        historiAwalAkademik.setKomentar("Menunggu respon dari bagian akademik.");
        historiAwalAkademik.setByBagian(ByBagian.AKADEMIK);
        historiAwalAkademik.setStatusApprove(bebasKewajiban.getStatusApproveAkademik());
        historiAwalAkademik.setUrutan(5);
        historiBebasKewajibanDao.save(historiAwalAkademik);
    }

    public void saveStaffPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user) {

        Integer jumlahKeterangan = kewajibanPerpusDao.countByStatus(StatusRecord.AKTIF).intValue();
        Integer jumlahdiCeklis = kewajibanPerpusMahasiswaDao
                .countByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF).intValue();
        log.info("jlm keterangan {} dan jml ceklis {}", jumlahKeterangan, jumlahdiCeklis);
        if (jumlahKeterangan == jumlahdiCeklis) {
            bebasKewajiban.setStatusApprovePerpus(StatusApprove.APPROVED);
            bebasKewajibanDao.save(bebasKewajiban);
            komentar = "DONE";

            // kirim email di approve
            mailService.mailApprovePerpus(bebasKewajiban, "DONE");
        } else {
            bebasKewajiban.setStatusApprovePerpus(StatusApprove.REJECTED);
            bebasKewajibanDao.save(bebasKewajiban);

            // kirim email di reject
            mailService.mailRejectPerpus(bebasKewajiban, komentar);
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.PERPUSTAKAAN);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApprovePerpus());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(1);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKabagPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user) {

        if (bebasKewajiban.getStatusApproveKabagPerpus() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KABAG_PERPUS);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKabagPerpus());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(2);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKeuangan(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user) {

        if (bebasKewajiban.getStatusApproveKeuangan() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KEUANGAN);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKeuangan());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(3);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveTlc(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user, String file) {

        if (bebasKewajiban.getStatusApproveTlc() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.TLC);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(4);
        historiBebasKewajiban.setFile(file);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveTqc(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user) {

        if (bebasKewajiban.getStatusApproveTlc() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.TQC);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(4);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveAkademik(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user) {

        if (bebasKewajiban.getStatusApproveAkademik() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.AKADEMIK);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveAkademik());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajiban.setUrutan(5);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public Integer nomorUrut() {
        Integer urutan;
        PendaftaranBebasKewajiban getNomorUrut = bebasKewajibanDao
                .findTopByTahunOrderByNomorUrutDesc(String.valueOf(LocalDate.now().getYear()));
        log.info("Bebas Kewajiban : {}", getNomorUrut);
        if (getNomorUrut.getNomorUrut() == null) {
            urutan = 1;
        } else {
            urutan = getNomorUrut.getNomorUrut() + 1;
        }
        log.info("Urut : {}", urutan);
        return urutan;
    }

    public String createNomorSurat(Integer nomorUrut, String tahun) {
        return String.format("%03d", nomorUrut) + "-B/KET/Tazkia-Universitas/Akd/" + LocalDate.now().getMonthValue()
                + "/" + tahun;
    }

    // ================== CETAK SKL (PDF) ==================
    public void cetakSKL(PendaftaranBebasKewajiban bebasKewajiban, OutputStream outputStream) throws IOException {
        System.out.println("cetakSKL berjalan");
        System.out.println("Logo path: " + logo);

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
        BiFunction<String, Float, Image> loadImage = (filename, scale) -> {
            try {
                String fullPath = logo + File.separator + filename;
                System.out.println("Loading image: " + fullPath);
                Image img = Image.getInstance(fullPath);
                img.scaleToFit(scale, scale);
                return img;
            } catch (Exception e) {
                System.err.println("Gagal load gambar: " + filename + " | Error: " + e.getMessage());
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

        // === JUDUL ===
        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 14);
        titleFont.setStyle(Font.UNDERLINE);
        Paragraph title = new Paragraph("SURAT KETERANGAN LULUS", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // === NOMOR SURAT ===
        Paragraph nomorSurat = new Paragraph(
                "Nomor: " + bebasKewajiban.getNomorSurat(),
                FontFactory.getFont(FontFactory.TIMES_ROMAN, 12));
        nomorSurat.setAlignment(Element.ALIGN_CENTER);
        document.add(nomorSurat);
        document.add(new Paragraph("\n\n"));

        // === GAMBAR SALAM ===
        Image salam = loadImage.apply("salam.png", 140f);
        if (salam != null) {
            Paragraph pSalam = new Paragraph();
            pSalam.setIndentationLeft(50);
            pSalam.add(new Chunk(salam, 0, -5, true));
            document.add(pSalam);
        }
        document.add(new Paragraph("\n\n"));

        // === ISI ===
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

        Paragraph p = new Paragraph(
                "Pimpinan Akademik Universitas Islam Tazkia dengan ini menerangkan bahwa :", fStandard);
        p.setAlignment(Element.ALIGN_JUSTIFIED);
        p.setIndentationLeft(50);
        p.setIndentationRight(50);
        document.add(p);
        document.add(new Paragraph("\n"));

        // === TABEL DATA MAHASISWA ===
        PdfPTable table = new PdfPTable(3);
        table.setWidths(new int[] { 10, 1, 15 });
        table.setWidthPercentage(80);
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);

        addTableCell(table, "Nama", font);
        addTableCell(table, ":", font);
        addTableCell(table, bebasKewajiban.getMahasiswa().getNama(), font);

        addTableCell(table, "NIM", font);
        addTableCell(table, ":", font);
        addTableCell(table, bebasKewajiban.getMahasiswa().getNim(), font);

        addTableCell(table, "Tempat, Tanggal Lahir", font);
        addTableCell(table, ":", font);
        addTableCell(table,
                bebasKewajiban.getMahasiswa().getTempatLahir() + ", " +
                        transcriptService.convertDateIndonesia(bebasKewajiban.getMahasiswa().getTanggalLahir()),
                font);
        document.add(table);
        document.add(new Paragraph("\n"));

        // === PARAGRAF LULUS ===
        Paragraph p2 = new Paragraph();
        p2.setAlignment(Element.ALIGN_JUSTIFIED);
        p2.setIndentationLeft(50);
        p2.setIndentationRight(50);
        p2.add(new Chunk(
                "adalah benar telah menyelesaikan semua persyaratan akademik dan telah mengikuti sidang tugas akhir sebagai ujian penutup pada Program Studi ",
                fStandard));
        p2.add(new Chunk(bebasKewajiban.getMahasiswa().getIdProdi().getNamaProdi(), fStandard));
        p2.add(new Chunk(", Fakultas ", fStandard));
        p2.add(new Chunk(bebasKewajiban.getMahasiswa().getIdProdi().getFakultas().getNamaFakultas(), fStandard));
        p2.add(new Chunk(" di Universitas Islam Tazkia dan telah dinyatakan ", fStandard));
        p2.add(new Chunk("LULUS ", fontB));
        p2.add(new Chunk(" untuk menyandang gelar " + bebasKewajiban.getMahasiswa().getIdProdi().getGelar(), fStandard));
        document.add(p2);
        document.add(new Paragraph("\n"));

        // === PENUTUP ===
        Paragraph p3 = new Paragraph(
                "Demikian, sementara menunggu ijazah asli, maka Surat Keterangan Lulus ini dapat dipergunakan sebagaimana mestinya.",
                fStandard);
        p3.setAlignment(Element.ALIGN_JUSTIFIED);
        p3.setIndentationLeft(50);
        p3.setIndentationRight(50);
        document.add(p3);
        document.add(new Paragraph("\n\n"));

        // === WASSALAM (dipindah ke atas tanggal) ===
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

        Paragraph jabatan = new Paragraph("Wakil Rektor Bidang Akademik,", fontB);
        jabatan.setSpacingAfter(10);
        leftCell.addElement(jabatan);

        try {
            String url = "https://smile.tazkia.ac.id/skl/" + bebasKewajiban.getId();
            BufferedImage qrImage = qrBarcode(url, 120);
            Image qrPdfImage = Image.getInstance(qrImage, null);
            qrPdfImage.scaleAbsolute(70, 70);
            leftCell.addElement(qrPdfImage);
        } catch (Exception e) {
            System.err.println("Gagal buat QR Code: " + e.getMessage());
            Paragraph fallback = new Paragraph("QR Error", FontFactory.getFont(FontFactory.TIMES_ROMAN, 8));
            fallback.setAlignment(Element.ALIGN_CENTER);
            leftCell.addElement(fallback);
        }

        Paragraph nama = new Paragraph("Dr. Nur Hendrasto, M.Si., CPC", fontB);
        nama.setSpacingBefore(5);
        leftCell.addElement(nama);

        Paragraph nidn = new Paragraph("NIK/NIDN: 018.156/2104028504", fontB);
        leftCell.addElement(nidn);

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
        System.out.println("PDF selesai dibuat!");
    }

    // === HELPER TABEL ===
    private void addTableCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private static BufferedImage qrBarcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);

        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE, size, size, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

}