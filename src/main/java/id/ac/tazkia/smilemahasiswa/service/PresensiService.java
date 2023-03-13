package id.ac.tazkia.smilemahasiswa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import id.ac.tazkia.smilemahasiswa.controller.EquipmentController;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class PresensiService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresensiService.class);

    @Autowired
    private TahunAkademikDao tahunAkademikDao;
    @Autowired
    private PresensiDosenDao presensiDosenDao;
    @Autowired
    private SesiKuliahDao sesiKuliahDao;
    @Autowired
    private NotifikasiService notifikasiService;
    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;
    @Autowired
    private KrsDetailDao krsDetailDao;
    @Autowired
    private JadwalDosenDao jadwalDosenDao;
    @Value("${presensi.barcode.url}")
    private String barcodeUrl;

    @Value("${upload.barcode}")
    private String uploadFolder;

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, String beritaAcara, LocalDateTime dateTime, String pertemuan)throws Exception {


        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(),j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);

        if (presensiDosen.getWaktuMasuk().toLocalTime().compareTo(presensiDosen.getJadwal().getJamMulai().plusMinutes(15)) >= 0) {
            presensiDosen.setStatusPresensi(StatusPresensi.TERLAMBAT);
            presensiDosenDao.save(presensiDosen);
//            notifikasiService.kirimNotifikasiTelat(presensiDosen);
            Integer qrCodeSize = 300;
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("ss");
            String productCode = dateFormat.format(date);


            Date date1 = Calendar.getInstance().getTime();
            DateFormat dateFormat1 = new SimpleDateFormat("mm");
            String productionYear = dateFormat1.format(date1);

            Date date2 = Calendar.getInstance().getTime();
            DateFormat dateFormat2 = new SimpleDateFormat("MM");
            String productCode2 = dateFormat2.format(date2);

            Integer productNumberLength = 6;
            Integer productQuantity = 1;

            String folderOutput = uploadFolder;
            new File(folderOutput).mkdirs();

            for (int i = 1; i <= productQuantity; i++) {
                String presensiBarcode = productCode + productionYear + productCode2
                        + String.format("%1$" + productNumberLength + "s", i)
                        .replace(' ', '2');
                System.out.println("Generate QR Code for product number " + presensiBarcode);
                BufferedImage qrCode = generateQRcode(barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
                ImageIO.write(qrCode, "png",
                        new File(folderOutput + File.separator
                                + presensiBarcode + ".png"));
                if (presensiDosen.getBarcodeNow() == null) {
                    presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                    presensiDosenDao.save(presensiDosen);
                } else {
                    File file = new File(uploadFolder + File.separator + presensiDosen.getBarcodeBefore());
                    file.delete();
                    presensiDosen.setBarcodeBefore(presensiDosen.getBarcodeNow());
                    presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                    presensiDosenDao.save(presensiDosen);

                }
            }

            SesiKuliah sesiKuliah = new SesiKuliah();
            sesiKuliah.setBeritaAcara(beritaAcara == null? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail){
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setStatusPresensi(StatusPresensi.HADIR);
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswa.setKrsDetail(kd);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now());
                presensiMahasiswa.setWaktuKeluar(sesiKuliah.getWaktuSelesai());
                presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
                presensiMahasiswa.setRating(0);
                presensiMahasiswaDao.save(presensiMahasiswa);
            }

        }else {
            presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
            presensiDosenDao.save(presensiDosen);

            Integer qrCodeSize = 300;
            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("ss");
            String productCode = dateFormat.format(date);


            Date date1 = Calendar.getInstance().getTime();
            DateFormat dateFormat1 = new SimpleDateFormat("mm");
            String productionYear = dateFormat1.format(date1);

            Date date2 = Calendar.getInstance().getTime();
            DateFormat dateFormat2 = new SimpleDateFormat("MM");
            String productCode2 = dateFormat2.format(date2);

            Integer productNumberLength = 6;
            Integer productQuantity = 1;

            String folderOutput = uploadFolder;
            new File(folderOutput).mkdirs();

            for (int i = 1; i <= productQuantity; i++) {
                String presensiBarcode = productCode + productionYear + productCode2
                        + String.format("%1$" + productNumberLength + "s", i)
                        .replace(' ', '2');
                System.out.println("Generate QR Code for product number " + presensiBarcode);
//                BufferedImage qrCode = generateQRcode("http://localhost:8080/presensi/" + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
            BufferedImage qrCode = generateQRcode(barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
                ImageIO.write(qrCode, "png",
                        new File(folderOutput + File.separator
                                + presensiBarcode + ".png"));
                if (presensiDosen.getBarcodeNow() == null) {
                    presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                    presensiDosenDao.save(presensiDosen);
                } else {
                    File file = new File(uploadFolder + File.separator + presensiDosen.getBarcodeBefore());
                    file.delete();
                    presensiDosen.setBarcodeBefore(presensiDosen.getBarcodeNow());
                    presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                    presensiDosenDao.save(presensiDosen);

                }
//            aset.setBarcode(productCode + productCode2 + "/" +productNumber + ".png");
            }

            SesiKuliah sesiKuliah = new SesiKuliah();
            sesiKuliah.setBeritaAcara(beritaAcara == null? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail){
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswa.setKrsDetail(kd);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
                presensiMahasiswa.setRating(0);
                presensiMahasiswaDao.save(presensiMahasiswa);
            }

        }


        return presensiDosen;
    }

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, LocalDateTime dateTime, String pertemuan)throws Exception{
        return inputPresensi(d, j, null, dateTime, pertemuan);
    }

    private static BufferedImage generateQRcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE,
                size, size, hints);
        BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(bitMatrix);

        int startingYposition = size - 2;

        Graphics2D g = (Graphics2D) qrCode.getGraphics();
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 5));
        Color color = Color.BLACK;
        g.setColor(color);

        FontMetrics fm = g.getFontMetrics();
        g.drawString(barcodeText, (qrCode.getWidth() / 2) - (fm.stringWidth(barcodeText) / 2), startingYposition);
        return qrCode;
    }

}
