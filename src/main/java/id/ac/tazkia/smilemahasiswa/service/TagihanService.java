package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.HapusTagihanRequest;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Local;
import org.bouncycastle.ocsp.Req;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@Slf4j
public class TagihanService {

    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String TAGIHAN_SEMPRO = "08";
    public static final String TAGIHAN_SKRIPSI = "09";
    public static final String TAGIHAN_UTS = "12";
    public static final String TAGIHAN_UAS = "13";
    public static final List<String> TAGIHAN_KRS = Arrays.asList("14", "22", "40");

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    public void requestCreateTagihan(Tagihan tagihan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(tagihan.getNilaiTagihan())
                .debitur(tagihan.getMahasiswa().getNim())
                .keterangan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " +tagihan.getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }

    public void requestCreateCicilan(RequestCicilan requestCicilan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(requestCicilan.getTagihan().getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(requestCicilan.getNilaiCicilan())
                .debitur(requestCicilan.getTagihan().getMahasiswa().getNim())
                .keterangan(requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " +requestCicilan.getTagihan().getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(requestCicilan.getTanggalJatuhTempo().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }


    public void hapusTagihan(Tagihan tagihan){
        HapusTagihanRequest hapusTagihan = HapusTagihanRequest.builder()
                .nomorTagihan(tagihan.getNomor())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .debitur(tagihan.getMahasiswa().getNim())
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getProdi().getKodeProdi())
                .build();
        kafkaSender.requsetHapusTagihan(hapusTagihan);
    }

    public void prosesPembayaran(Tagihan tagihan, PembayaranTagihan pt){

        BigDecimal akumulasi = tagihan.getAkumulasiPembayaran().add(pt.getNilaiPembayaran());
        BigDecimal nilai = tagihan.getNilaiTagihan();
        tagihan.setAkumulasiPembayaran(akumulasi);
        log.info("akumulasi : {}", akumulasi);
        if (akumulasi.compareTo(nilai) == 0){
            tagihan.setLunas(true);
            tagihan.setStatusTagihan(StatusTagihan.LUNAS);
            tagihanDao.save(tagihan);
            log.info("nomor tagihan {} LUNAS", tagihan.getNomor());
        }

        log.debug("Pembayaran Tagihan = {}", pt.toString());

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTagihan(tagihan);
        pembayaran.setNomorRekening(pt.getNomorRekening());
        pembayaran.setAmount(pt.getNilaiPembayaran());
        pembayaran.setWaktuBayar(LocalDateTime.parse(pt.getWaktuPembayaran(), FORMATTER_ISO_DATE_TIME));
        pembayaran.setReferensi(pt.getReferensiPembayaran());

        Bank bank = new Bank();
        bank.setId(pt.getBank());
        pembayaran.setBank(bank);

        if (TAGIHAN_UTS.equals(pt.getKodeJenisBiaya().equals(TAGIHAN_UTS))){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.UTS, false, tagihan.getTahunAkademik());
            if (enableFiture != null){
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        if (TAGIHAN_UAS.equals(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.UAS, false, tagihan.getTahunAkademik());
            if (enableFiture != null){
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        if (TAGIHAN_KRS.contains(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.KRS, false, tagihan.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.KRS);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
            }
            enableFiture.setEnable(true);
            enableFitureDao.save(enableFiture);

            TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tagihan.getTahunAkademik(),
                    tagihan.getMahasiswa().getIdProdi());

            Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(
                    tagihan.getMahasiswa(), tagihan.getTahunAkademik(), StatusRecord.AKTIF);

            if(krs == null) {
                createKrs(tagihan, tahunAkademikProdi);
            }
        }

        if (TAGIHAN_SEMPRO.equals(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.SEMPRO, true, tagihan.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.SEMPRO);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        if (TAGIHAN_SKRIPSI.equals(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.SKRIPSI, true, tagihan.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.SKRIPSI);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);

        log.debug("Pembayaran untuk tagihan {} berhasil disimpan", pt.getNomorTagihan());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate hariIni = LocalDate.now();
//        LocalDate tgl10Bulandepan = hariIni.plusMonths(1).withDayOfMonth(10);

        RequestCicilan cicilanLunas = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tagihan, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);
        cicilanLunas.setStatusCicilan(StatusCicilan.LUNAS);
        requestCicilanDao.save(cicilanLunas);
        log.info("cicilan lunas : {}", cicilanLunas);


        RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(tagihan);
        if (requestCicilan == null){
            log.info("tidak ada cicilan");
        }
        requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
        requestCicilanDao.save(requestCicilan);
        requestCreateCicilan(requestCicilan);
        log.info("kirim cicilan selanjutnya : {}", requestCicilan);

    }

    public void createKrs(Tagihan tagihan, TahunAkademikProdi tahunAkademikProdi) {
        Krs krs = new Krs();
        krs.setTahunAkademik(tagihan.getTahunAkademik());
        krs.setTahunAkademikProdi(tahunAkademikProdi);
        krs.setProdi(tagihan.getMahasiswa().getIdProdi());
        krs.setMahasiswa(tagihan.getMahasiswa());
        krs.setNim(tagihan.getMahasiswa().getNim());
        krs.setTanggalTransaksi(LocalDateTime.now());
        krs.setStatus(StatusRecord.AKTIF);
        krsDao.save(krs);
    }

}
