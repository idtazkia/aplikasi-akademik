package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
@Slf4j
public class TagihanService {

    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
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
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    public void createTagihan(Tagihan tagihan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(tagihan.getNilaiTagihan())
                .debitur(tagihan.getMahasiswa().getNim())
                .keterangan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " +tagihan.getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }

    public void prosesPembayaran(Tagihan tagihan, PembayaranTagihan pt){
        tagihan.setLunas(true);
        tagihan.setStatusTagihan(StatusTagihan.LUNAS);
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

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);
        log.debug("Pembayaran untuk tagihan {} berhasil disimpan", pt.getNomorTagihan());

    }

}
