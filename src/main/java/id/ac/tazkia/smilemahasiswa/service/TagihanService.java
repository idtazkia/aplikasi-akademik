package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.Bidi;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@Transactional
public class TagihanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagihanService.class);
    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

        VirtualAccount va = virtualAccountDao.findByBankIdAndTagihan(pt.getBank(), tagihan);

        System.out.println("Pembayaran Tagihan = " + pt);

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTagihan(tagihan);
        pembayaran.setNomorRekening(va.getNomor());
        pembayaran.setAmount(pt.getNilaiPembayaran());
        pembayaran.setWaktuBayar(LocalDateTime.parse(pt.getWaktuPembayaran(), FORMATTER_ISO_DATE_TIME));
        pembayaran.setReferensi(pt.getReferensiPembayaran());

        Bank bank = new Bank();
        bank.setId(pt.getBank());
        pembayaran.setBank(bank);

        if (tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode() == "12"){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(), StatusRecord.UTS, "0", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (enableFiture != null){
                enableFiture.setEnable("1");
                enableFitureDao.save(enableFiture);
            }
        }

        if (tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode() == "13"){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(), StatusRecord.UAS, "0", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (enableFiture != null){
                enableFiture.setEnable("1");
                enableFitureDao.save(enableFiture);
            }
        }

        if (tagihan.getNilaiJenisTagihan().getJenisTagihan().getKode() == "14"){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(), StatusRecord.KRS, "0", tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
            if (enableFiture != null){
                enableFiture.setEnable("1");
                enableFitureDao.save(enableFiture);
            }
        }

        TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tagihan.getTahunAkademik(), tagihan.getMahasiswa().getIdProdi());
        Krs krs = new Krs();
        krs.setTahunAkademik(tagihan.getTahunAkademik());
        krs.setTahunAkademikProdi(tahunAkademikProdi);
        krs.setProdi(tagihan.getMahasiswa().getIdProdi());
        krs.setMahasiswa(tagihan.getMahasiswa());
        krs.setNim(tagihan.getMahasiswa().getNim());
        krs.setTanggalTransaksi(LocalDateTime.now());
        krs.setStatus(StatusRecord.AKTIF);
        krsDao.save(krs);

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);
        LOGGER.debug("Pembayaran untuk tagihan {} berhasil disimpan", pt.getNomorTagihan());

    }

}
