package id.ac.tazkia.smilemahasiswa.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.CMYKColor;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import id.ac.tazkia.smilemahasiswa.dao.BebasKewajibanDao;
import id.ac.tazkia.smilemahasiswa.dao.HistoriBebasKewajibanDao;
import id.ac.tazkia.smilemahasiswa.dao.KewajibanPerpusDao;
import id.ac.tazkia.smilemahasiswa.dao.KewajibanPerpusMahasiswaDao;
import id.ac.tazkia.smilemahasiswa.entity.BebasKewajiban.*;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.StatusApprove;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
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
@Transactional
public class BebasKewajibanService {

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
    TranscriptService transcriptService;

    public void saveDaftarKewajiban(Mahasiswa mahasiswa){

        PendaftaranBebasKewajiban cek = bebasKewajibanDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
        if (cek == null) {
            PendaftaranBebasKewajiban kewajiban = new PendaftaranBebasKewajiban();
            kewajiban.setMahasiswa(mahasiswa);
            bebasKewajibanDao.save(kewajiban);

            List<KewajibanPerpus> listKewajiban = kewajibanPerpusDao.findByStatusOrderById(StatusRecord.AKTIF);
            for (KewajibanPerpus list : listKewajiban){
                KewajibanPerpusMahasiswa perpusMahasiswa = new KewajibanPerpusMahasiswa();
                perpusMahasiswa.setKewajibanPerpus(list);
                perpusMahasiswa.setBebasKewajiban(kewajiban);
                kewajibanPerpusMahasiswaDao.save(perpusMahasiswa);
            }
        }
    }

    public void saveStaffPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar){

        Integer jumlahKeterangan = kewajibanPerpusDao.countByStatus(StatusRecord.AKTIF).intValue();
        Integer jumlahdiCeklis = kewajibanPerpusMahasiswaDao.countByBebasKewajibanAndStatus(bebasKewajiban, StatusRecord.AKTIF).intValue();
        if (jumlahKeterangan == jumlahdiCeklis) {
            bebasKewajiban.setStatusApprovePerpus(StatusApprove.APPROVED);
            bebasKewajibanDao.save(bebasKewajiban);
            komentar = "DONE";
        }else{
            bebasKewajiban.setStatusApprovePerpus(StatusApprove.WAITING);
            bebasKewajibanDao.save(bebasKewajiban);
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.PERPUSTAKAAN);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApprovePerpus());
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKabagPerpus(PendaftaranBebasKewajiban bebasKewajiban, String komentar){

        if (bebasKewajiban.getStatusApproveKabagPerpus() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KABAG_PERPUS);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKabagPerpus());
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveKeuangan(PendaftaranBebasKewajiban bebasKewajiban, String komentar){

        if (bebasKewajiban.getStatusApproveKeuangan() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.KEUANGAN);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveKeuangan());
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveTlc(PendaftaranBebasKewajiban bebasKewajiban, String komentar){

        if (bebasKewajiban.getStatusApproveTlc() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.TLC);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveTlc());
        historiBebasKewajibanDao.save(historiBebasKewajiban);

    }

    public void saveAkademik(PendaftaranBebasKewajiban bebasKewajiban, String komentar){

        if (bebasKewajiban.getStatusApproveAkademik() == StatusApprove.APPROVED) {
            komentar = "Done";
        }

        HistoriBebasKewajiban historiBebasKewajiban = new HistoriBebasKewajiban();
        historiBebasKewajiban.setBebasKewajiban(bebasKewajiban);
        historiBebasKewajiban.setKomentar(komentar);
        historiBebasKewajiban.setByBagian(ByBagian.AKADEMIK);
        historiBebasKewajiban.setStatusApprove(bebasKewajiban.getStatusApproveAkademik());
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