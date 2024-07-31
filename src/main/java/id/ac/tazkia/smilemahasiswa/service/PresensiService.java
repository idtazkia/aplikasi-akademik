package id.ac.tazkia.smilemahasiswa.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.attendance.PresensiDto;
import id.ac.tazkia.smilemahasiswa.dto.response.BaseResponse;
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
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
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
    @Autowired
    private JadwalDao jadwalDao;
    @Value("${presensi.barcode.url}")
    private String barcodeUrl;

    @Value("${upload.barcode}")
    private String uploadFolder;

    public PresensiDosen inputPresensiPengganti(Dosen d, Jadwal j, String beritaAcara, LocalDateTime dateTime,
            String pertemuan, Sesi sesi) throws Exception {

        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(), sesi.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);

        if (presensiDosen.getWaktuMasuk().toLocalTime().compareTo(sesi.getJamMulai().plusMinutes(15)) >= 0) {
            presensiDosen.setStatusPresensi(StatusPresensi.TERLAMBAT);
            presensiDosenDao.save(presensiDosen);
            // notifikasiService.kirimNotifikasiTelat(presensiDosen);
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
                BufferedImage qrCode = generateQRcode(
                        barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
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
            sesiKuliah.setBeritaAcara(beritaAcara == null ? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,
                    StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
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
                updateNilaiPresensi(kd);
            }

        } else {
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
                // BufferedImage qrCode = generateQRcode("http://localhost:8080/presensi/" +
                // presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
                BufferedImage qrCode = generateQRcode(
                        barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
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
                // aset.setBarcode(productCode + productCode2 + "/" +productNumber + ".png");
            }

            SesiKuliah sesiKuliah = new SesiKuliah();
            sesiKuliah.setBeritaAcara(beritaAcara == null ? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,
                    StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswa.setKrsDetail(kd);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
                presensiMahasiswa.setRating(0);
                presensiMahasiswaDao.save(presensiMahasiswa);
                updateNilaiPresensi(kd);
            }

        }

        return presensiDosen;
    }

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, String beritaAcara, LocalDateTime dateTime, String pertemuan)
            throws Exception {

        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(), j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);

        if (presensiDosen.getWaktuMasuk().toLocalTime()
                .compareTo(presensiDosen.getJadwal().getJamMulai().plusMinutes(15)) >= 0) {
            presensiDosen.setStatusPresensi(StatusPresensi.TERLAMBAT);
            presensiDosenDao.save(presensiDosen);
            // notifikasiService.kirimNotifikasiTelat(presensiDosen);
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
                BufferedImage qrCode = generateQRcode(
                        barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
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
            sesiKuliah.setBeritaAcara(beritaAcara == null ? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,
                    StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
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
                updateNilaiPresensi(kd);
            }

        } else {
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
                // BufferedImage qrCode = generateQRcode("http://localhost:8080/presensi/" +
                // presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
                BufferedImage qrCode = generateQRcode(
                        barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
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
                // aset.setBarcode(productCode + productCode2 + "/" +productNumber + ".png");
            }

            SesiKuliah sesiKuliah = new SesiKuliah();
            sesiKuliah.setBeritaAcara(beritaAcara == null ? "" : beritaAcara.trim());
            sesiKuliah.setJadwal(j);
            sesiKuliah.setPresensiDosen(presensiDosen);
            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliah.setPertemuan(pertemuan);
            sesiKuliahDao.save(sesiKuliah);

            Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,
                    StatusRecord.AKTIF);
            for (KrsDetail kd : krsDetail) {
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswa.setKrsDetail(kd);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
                presensiMahasiswa.setRating(0);
                presensiMahasiswaDao.save(presensiMahasiswa);
                updateNilaiPresensi(kd);
            }

        }

        return presensiDosen;
    }

    public PresensiDosen generatePresensi(Dosen d, Jadwal j, String beritaAcara, LocalDateTime dateTime,
            Integer pertemuan) throws Exception {

        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(d);
        presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(), j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
        presensiDosen.setJadwal(j);
        presensiDosen.setStatusPresensi(StatusPresensi.BELUM_MENGAJAR);
        presensiDosen.setStatus(StatusRecord.NONAKTIF);
        presensiDosenDao.save(presensiDosen);

        SesiKuliah sesiKuliah = new SesiKuliah();
        sesiKuliah.setBeritaAcara(beritaAcara);
        sesiKuliah.setJadwal(j);
        sesiKuliah.setPresensiDosen(presensiDosen);
        sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
        sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
        sesiKuliah.setPertemuan("Belum Mengajar");
        sesiKuliah.setPertemuanKe(pertemuan);
        sesiKuliahDao.save(sesiKuliah);

        j.setFinalPloting("FINISHED");
        jadwalDao.save(j);

        return presensiDosen;
    }

    public void updateTanggalPresensi(Dosen d, Jadwal j, LocalDateTime dateTime,
            Integer pertemuan) throws Exception {

        SesiKuliah sesiKuliah = sesiKuliahDao.findByJadwalAndPertemuanKe(j, pertemuan).get();
        PresensiDosen presensiDosen = sesiKuliah.getPresensiDosen();
        if (presensiDosen.getStatusPresensi().equals(StatusPresensi.BELUM_MENGAJAR)) {
            presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(), j.getJamSelesai()));
            presensiDosen.setWaktuMasuk(dateTime);
            presensiDosen.setDosen(d);
            presensiDosenDao.save(presensiDosen);

            sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
            sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
            sesiKuliahDao.save(sesiKuliah);
        }

    }

    public void regenerateTanggal(Dosen d, Jadwal j, LocalDateTime dateTime,
            Integer pertemuan) throws Exception {

        SesiKuliah sesiKuliah = sesiKuliahDao.findByJadwalAndPertemuanKe(j, pertemuan).get();
        PresensiDosen presensiDosen = sesiKuliah.getPresensiDosen();

        presensiDosen.setWaktuSelesai(LocalDateTime.of(dateTime.toLocalDate(), j.getJamSelesai()));
        presensiDosen.setWaktuMasuk(dateTime);
        presensiDosen.setDosen(d);
        presensiDosenDao.save(presensiDosen);

        sesiKuliah.setWaktuMulai(presensiDosen.getWaktuMasuk());
        sesiKuliah.setWaktuSelesai(presensiDosen.getWaktuSelesai());
        sesiKuliahDao.save(sesiKuliah);

    }

    public void updatePresensi(Jadwal j) {

        Integer valueHari = Integer.valueOf(j.getHari().getId());
        if (j.getHari().getId().equals("0")) {
            valueHari = 7;
        }
        List<PresensiDosen> presensiDosenList = presensiDosenDao.findByJadwalAndStatusNotInAndStatusPresensi(j,
                Arrays.asList(StatusRecord.HAPUS), StatusPresensi.BELUM_MENGAJAR);
        for (PresensiDosen presensiDosen : presensiDosenList) {
            System.out.println(presensiDosen.getWaktuMasuk().getDayOfWeek().getValue() - valueHari);
        }
    }

    public PresensiDosen inputPresensi(Dosen d, Jadwal j, LocalDateTime dateTime, String pertemuan) throws Exception {
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

    public BaseResponse generateSesi(Jadwal jadwal) throws Exception {
        BaseResponse response = null;
        Long totalPresensi = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF, jadwal);
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = jadwal.getTahunAkademik().getTanggalMulaiKuliah().plusDays(valueJadwal - 1);

        if (totalPresensi == 0) {
            int attend = 16;

            for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

                generatePresensi(jadwal.getDosen(),
                        jadwal, "-",
                        LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                        pertemuan);

            }
        }
        response = BaseResponse.builder()
                .code("200").message("Berhasil Generate")
                .build();

        return response;

    }

    public void generateSesi2(Jadwal jadwal) throws Exception {
        Long totalPresensi = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF, jadwal);
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = jadwal.getTahunAkademik().getTanggalMulaiKuliah().plusDays(valueJadwal - 1);

        if (totalPresensi == 0) {
            int attend = 16;

            for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

                generatePresensi(jadwal.getDosen(),
                        jadwal, "-",
                        LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                        pertemuan);

            }
        }
    }

    public void updateSesi1(Jadwal jadwal) throws Exception {
        Long totalPresensi = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF, jadwal);
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = jadwal.getTahunAkademik().getTanggalMulaiKuliah().plusDays(valueJadwal - 1);

        int attend = 16;

        for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

            updateTanggalPresensi(jadwal.getDosen(),
                    jadwal,
                    LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                    pertemuan);

        }

    }

    public void reGenerate(Jadwal jadwal, String tanggal) throws Exception {
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = LocalDate.parse(tanggal).plusDays(valueJadwal - 1);

        int attend = 16;

        for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

            regenerateTanggal(jadwal.getDosen(),
                    jadwal,
                    LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                    pertemuan);

        }

    }

    public void reGenerate1(Jadwal jadwal) throws Exception {
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = LocalDate.parse("2023-10-16").plusDays(valueJadwal - 1);

        int attend = 16;

        for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

            regenerateTanggal(jadwal.getDosen(),
                    jadwal,
                    LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                    pertemuan);

        }

    }

    public BaseResponse updateSesi(Jadwal jadwal) throws Exception {
        Long totalPresensi = presensiDosenDao.countByStatusAndJadwal(StatusRecord.AKTIF, jadwal);
        Integer valueJadwal = null;
        if (jadwal.getHari().getId().equals("0")) {
            valueJadwal = 7;
        } else {
            valueJadwal = Integer.parseInt(jadwal.getHari().getId());
        }

        LocalDate firstAttendDate = jadwal.getTahunAkademik().getTanggalMulaiKuliah().plusDays(valueJadwal - 1);

        if (totalPresensi == 0) {
            int attend = 16;

            for (int pertemuan = 1; pertemuan <= attend; pertemuan++) {

                updateTanggalPresensi(jadwal.getDosen(),
                        jadwal, LocalDateTime.of(firstAttendDate.plusWeeks(pertemuan - 1), jadwal.getJamMulai()),
                        pertemuan);

            }
        }
        BaseResponse response = BaseResponse.builder()
                .code("200").message("Berhasil Generate")
                .build();

        return response;
    }

    public BaseResponse generatePresensiMangkir(SesiKuliah sesiKuliah) throws Exception {

        sesiKuliah.setBeritaAcara("Otomatis Mangkir, karena tidak absen");
        sesiKuliah.setPertemuan("Mangkir");
        sesiKuliahDao.save(sesiKuliah);

        PresensiDosen presensiDosen = sesiKuliah.getPresensiDosen();
        presensiDosen.setStatusPresensi(StatusPresensi.MANGKIR);
        presensiDosen.setStatus(StatusRecord.AKTIF);
        presensiDosenDao.save(presensiDosen);

        Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(
                presensiDosen.getJadwal(),
                StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail) {
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setCatatan("System");
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setRating(0);
            presensiMahasiswaDao.save(presensiMahasiswa);
        }

        return BaseResponse.builder()
                .code("200").message("Berhasil Generate")
                .build();
    }

    public void mulaiMengajar(PresensiDto jadwalDto, SesiKuliah sesiKuliah, LocalDateTime waktuMasukSebelum)
            throws Exception {
        if (jadwalDto.getJamMulai() == null) {
            jadwalDto.setJamMulai(LocalTime.now());
        }

        sesiKuliah.setBeritaAcara(jadwalDto.getBeritaAcara());
        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(), jadwalDto.getJamMulai().plusHours(7)));
        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(), sesiKuliah.getJadwal().getJamSelesai()));
        sesiKuliah.setDetailPertemuan(jadwalDto.getDetailPertemuan());
        sesiKuliah.setTanggalInput(LocalDateTime.now());
        sesiKuliah.setCeramah(jadwalDto.getCeramah());
        sesiKuliah.setDiskusi(jadwalDto.getDiskusi());
        sesiKuliah.setPresentasi(jadwalDto.getPresentasi());
        sesiKuliah.setTanyajawab(jadwalDto.getTanyajawab());
        sesiKuliah.setKuis(jadwalDto.getKuis());

        PresensiDosen presensiDosen = presensiDosenDao.findById(sesiKuliah.getPresensiDosen().getId()).get();
        presensiDosen.setDosen(jadwalDto.getDosen());
        presensiDosen.setWaktuMasuk(sesiKuliah.getWaktuMulai());
        presensiDosen.setWaktuSelesai(sesiKuliah.getWaktuSelesai());
        presensiDosen.setStatus(StatusRecord.AKTIF);
        presensiDosen.setTanggalInput(LocalDateTime.now());

        if (waktuMasukSebelum.compareTo(sesiKuliah.getWaktuMulai()) == 0) {
            if (presensiDosen.getWaktuMasuk().toLocalTime()
                    .compareTo(presensiDosen.getWaktuMasuk().toLocalTime().plusMinutes(15)) >= 0) {
                presensiDosen.setStatusPresensi(StatusPresensi.TERLAMBAT);
            } else {
                presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
            }
            sesiKuliah.setPertemuan(jadwalDto.getPertemuan());
        } else {
            presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
        }

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
            BufferedImage qrCode = generateQRcode(
                    barcodeUrl + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
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

        sesiKuliah.setPertemuan(jadwalDto.getPertemuan());
        sesiKuliah.setLinkVideo(jadwalDto.getLinkVideo());
        sesiKuliahDao.save(sesiKuliah);
        presensiDosenDao.save(presensiDosen);

        StatusPresensi statusMahasiswa = null;
        if (presensiDosen.getStatusPresensi() == StatusPresensi.HADIR) {
            statusMahasiswa = StatusPresensi.MANGKIR;
        } else {
            statusMahasiswa = StatusPresensi.HADIR;
        }

        Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(
                presensiDosen.getJadwal(),
                StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail) {
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setStatusPresensi(statusMahasiswa);
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setCatatan("Manual");
            presensiMahasiswa.setWaktuMasuk(LocalDateTime.now().plusHours(7));
            presensiMahasiswa.setWaktuKeluar(sesiKuliah.getWaktuSelesai());
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setRating(0);
            presensiMahasiswaDao.save(presensiMahasiswa);
            updateNilaiPresensi(kd);
        }
    }

    public BaseResponse generatePresensiMahasiswa(SesiKuliah sesiKuliah) throws Exception {

        Iterable<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(
                sesiKuliah.getJadwal(),
                StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail) {
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setCatatan("System");
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setRating(0);
            presensiMahasiswaDao.save(presensiMahasiswa);
        }

        return BaseResponse.builder()
                .code("200").message("Berhasil Generate")
                .build();
    }

    public void updateNilaiPresensi(KrsDetail kd){
        Integer presensiDosen = presensiDosenDao.countByJadwalAndStatusAndStatusPresensi(kd.getJadwal(), StatusRecord.AKTIF, StatusPresensi.HADIR);
        Integer presensiMhs = presensiMahasiswaDao.countByStatusPresensiNotInAndStatusAndKrsDetail(Arrays.asList(StatusPresensi.MANGKIR, StatusPresensi.TERLAMBAT), StatusRecord.AKTIF, kd);

        kd.setNilaiPresensi(new BigDecimal((presensiMhs * 100) / presensiDosen));
        kd.setJumlahKehadiran(presensiMhs);
        krsDetailDao.save(kd);

    }

}
