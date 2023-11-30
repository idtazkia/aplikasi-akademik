package id.ac.tazkia.smilemahasiswa.service;

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
import java.util.List;

@Service
@Transactional @Slf4j
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

    public void saveDaftarKewajiban(Mahasiswa mahasiswa){

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
            for (KewajibanPerpus list : listKewajiban){
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

    public void kirimEmailAdmin(PendaftaranBebasKewajiban bebasKewajiban){

        // Admin perpus
        List<User> perpus = userDao.findByRoleId("perpustakaan");
        for (User u : perpus){
            mailService.templateNotifAdmin(bebasKewajiban, "perpustakaan", u.getUsername());
        }
        List<User> kabagperpus = userDao.findByRoleId("kabagperpus");
        for (User u : kabagperpus){
            mailService.templateNotifAdmin(bebasKewajiban, "perpustakaan", u.getUsername());
        }

        // Admin keuangan
        mailService.templateNotifAdmin(bebasKewajiban, "keuangan", "ahmadrisan@tazkia.ac.id");
        mailService.templateNotifAdmin(bebasKewajiban, "keuangan", "mfurqonh@tazkia.ac.id");

        // Admin TLC
        List<User> stafTlc = userDao.findByRoleId("staftlc");
        for (User u : stafTlc){
            mailService.templateNotifAdmin(bebasKewajiban, "tlc", u.getUsername());
        }
        List<User> tlc = userDao.findByRoleId("tlc");
        for (User u : tlc){
            mailService.templateNotifAdmin(bebasKewajiban, "tlc", u.getUsername());
        }

        // Admin akademik
        mailService.templateNotifAdmin(bebasKewajiban, "akademik", "riska@tazkia.ac.id");
        mailService.templateNotifAdmin(bebasKewajiban, "akademik", "anaminuri@tazkia.ac.id");
    }

    public void createFirstHistoriApproval(PendaftaranBebasKewajiban bebasKewajiban){
        // bagian staff perpus
        HistoriBebasKewajiban historiAwalPerpus = new HistoriBebasKewajiban();
        historiAwalPerpus.setBebasKewajiban(bebasKewajiban);
        historiAwalPerpus.setKomentar("Menunggu respon dari bagian staf perpus.");
        historiAwalPerpus.setByBagian(ByBagian.PERPUSTAKAAN);
        historiAwalPerpus.setStatusApprove(bebasKewajiban.getStatusApprovePerpus());
        historiBebasKewajibanDao.save(historiAwalPerpus);
        // bagian Kabag perpus
        HistoriBebasKewajiban historiAwalKabagPerpus = new HistoriBebasKewajiban();
        historiAwalKabagPerpus.setBebasKewajiban(bebasKewajiban);
        historiAwalKabagPerpus.setKomentar("Menunggu respon dari kepala bagian perpus.");
        historiAwalKabagPerpus.setByBagian(ByBagian.KABAG_PERPUS);
        historiAwalKabagPerpus.setStatusApprove(bebasKewajiban.getStatusApproveKabagPerpus());
        historiBebasKewajibanDao.save(historiAwalKabagPerpus);
        // bagian keuangan
        HistoriBebasKewajiban historiAwalKeuangan = new HistoriBebasKewajiban();
        historiAwalKeuangan.setBebasKewajiban(bebasKewajiban);
        historiAwalKeuangan.setKomentar("Menunggu respon dari bagian keuangan.");
        historiAwalKeuangan.setByBagian(ByBagian.KEUANGAN);
        historiAwalKeuangan.setStatusApprove(bebasKewajiban.getStatusApproveKeuangan());
        historiBebasKewajibanDao.save(historiAwalKeuangan);
        // bagian staff TLC
        HistoriBebasKewajiban historiAwalTlc = new HistoriBebasKewajiban();
        historiAwalTlc.setBebasKewajiban(bebasKewajiban);
        historiAwalTlc.setKomentar("Menunggu respon dari bagian TLC.");
        historiAwalTlc.setByBagian(ByBagian.TLC);
        historiAwalTlc.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiBebasKewajibanDao.save(historiAwalTlc);
        // bagian staff Akademik
        HistoriBebasKewajiban historiAwalAkademik = new HistoriBebasKewajiban();
        historiAwalAkademik.setBebasKewajiban(bebasKewajiban);
        historiAwalAkademik.setKomentar("Menunggu respon dari bagian akademik.");
        historiAwalAkademik.setByBagian(ByBagian.AKADEMIK);
        historiAwalAkademik.setStatusApprove(bebasKewajiban.getStatusApproveAkademik());
        historiBebasKewajibanDao.save(historiAwalAkademik);
    }

    public void saveStaffPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user){

        Integer jumlahKeterangan = kewajibanPerpusDao.countByStatus(StatusRecord.AKTIF).intValue();
        Integer jumlahdiCeklis = kewajibanPerpusMahasiswaDao.countByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF).intValue();
        log.info("jlm keterangan {} dan jml ceklis {}", jumlahKeterangan, jumlahdiCeklis);
        if (jumlahKeterangan == jumlahdiCeklis) {
            bebasKewajiban.setStatusApprovePerpus(StatusApprove.APPROVED);
            bebasKewajibanDao.save(bebasKewajiban);
            komentar = "DONE";

            // kirim email di  approve
            mailService.mailApprovePerpus(bebasKewajiban, "DONE");
        }else{
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
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKabagPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user){

        if (bebasKewajiban.getStatusApproveKabagPerpus() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KABAG_PERPUS);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKabagPerpus());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKeuangan(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user){

        if (bebasKewajiban.getStatusApproveKeuangan() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KEUANGAN);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKeuangan());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveTlc(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user){

        if (bebasKewajiban.getStatusApproveTlc() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.TLC);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveAkademik(PendaftaranBebasKewajiban bebasKewajiban, String komentar, User user){

        if (bebasKewajiban.getStatusApproveAkademik() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.AKADEMIK);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveAkademik());
        historiBebasKewajiban.setUser(user);
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }


    public void cetakSKL(@RequestParam PendaftaranBebasKewajiban bebasKewajiban, HttpServletResponse response) throws IOException {

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());

        document.open();

        Image background = Image.getInstance(logo + File.separator + "LOGO TR.png");
        background.setAbsolutePosition(400, 190);
        background.scaleAbsolute(400, 450);
        document.add(background);

        Font titleFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        titleFont.setStyle(Font.UNDERLINE);
        Font kodeFont = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);
        Font fStandard = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12);
        Font fontB = FontFactory.getFont(FontFactory.TIMES_BOLD, 12);

        Paragraph paragrafkosong = new Paragraph("");

        Image img = Image.getInstance(logo + File.separator + "bismillah.png");
//        img.setWidthPercentage(20);
        img.scaleToFit(140, 140);
        img.setAbsolutePosition(222, PageSize.A4.getHeight() - (img.getScaledHeight()) - 90);
        Image lTazkia = Image.getInstance(logo + File.separator + "LOGO.png");
        lTazkia.scaleToFit(100, 100);
        lTazkia.setAbsolutePosition(20, PageSize.A4.getHeight() - (lTazkia.getScaledHeight()) - 15);
        Paragraph newLine = new Paragraph("\n\n\n\n\n");
        document.add(img);
        document.add(lTazkia);
        document.add(newLine);

        Paragraph titleParagraph = new Paragraph("SURAT KETERANGAN LULUS", titleFont);
        titleParagraph.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(titleParagraph);

        Paragraph nLine = new Paragraph("\n\n\n\n");
        Image salam = Image.getInstance(logo + File.separator + "salam.png");
        salam.scaleToFit(140, 140);
        salam.setAbsolutePosition(85, PageSize.A4.getHeight() - (salam.getScaledHeight()) - 190);
        document.add(salam);
        document.add(nLine);

        paragrafkosong = new Paragraph("Pimpinan Akademik Institut Agama Islam Tazkia dengan ini menerangkan bahwa :", fStandard);
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(70);
        table.setWidths(new int[] {10,1,15});
        table.setSpacingBefore(10);
        Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN);
        font.setColor(CMYKColor.BLACK);

        PdfPCell cell;
        cell = new PdfPCell(new Phrase("Nama",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(bebasKewajiban.getMahasiswa().getNama(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("NIM",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(bebasKewajiban.getMahasiswa().getNim(), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase("Tempat, Tanggal Lahir",font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(":", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
        cell = new PdfPCell(new Phrase(bebasKewajiban.getMahasiswa().getTempatLahir()+", " + transcriptService.convertDateIndonesia(bebasKewajiban.getMahasiswa().getTanggalLahir()), font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
//        cell = new PdfPCell(new Phrase("Unit Kerja",font));
//        cell.setBorder(Rectangle.NO_BORDER);
//        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        table.addCell(cell);
//        cell = new PdfPCell(new Phrase(":", font));
//        cell.setBorder(Rectangle.NO_BORDER);
//        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        table.addCell(cell);
//        cell = new PdfPCell(new Phrase("Institut Agama Islam Tazkia", font));
//        cell.setBorder(Rectangle.NO_BORDER);
//        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
//        table.addCell(cell);

        document.add(table);

        paragrafkosong = new Paragraph();
        paragrafkosong.add(new Chunk("adalah benar telah menyelesaikan semua persyaratan akademik dan telah mengikuti sidang tugas akhir sebagai ujian penutup pada Program Studi ", fStandard));
        paragrafkosong.add(new Chunk(bebasKewajiban.getMahasiswa().getIdProdi().getNamaProdi(), fStandard));
        paragrafkosong.add(new Chunk(", Fakultas ", fStandard));
        paragrafkosong.add(new Chunk(bebasKewajiban.getMahasiswa().getIdProdi().getFakultas().getNamaFakultas(), fStandard));
        paragrafkosong.add(new Chunk(" di Institut Agama Islam Tazkia dan telah dinyatakan ", fStandard));
        paragrafkosong.add(new Chunk("LULUS ", fontB));
        paragrafkosong.add(new Chunk(" untuk menyandang gelar ", fStandard));
//        paragrafkosong.add(new Chunk(" sebagai berikut:", fStandard));
        paragrafkosong.setAlignment(Element.ALIGN_BASELINE);
        paragrafkosong.setIndentationLeft(50);
        document.add(paragrafkosong);

        document.close();

    }

}