package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import id.ac.tazkia.smilemahasiswa.dao.PembayaranDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dao.VirtualAccountDao;
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
        pembayaran.setWaktuBayar(LocalDate.parse(pt.getWaktuPembayaran(), FORMATTER_ISO_DATE_TIME));
        pembayaran.setReferensi(pt.getReferensiPembayaran());

        Bank bank = new Bank();
        bank.setId(pt.getBank());
        pembayaran.setBank(bank);

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);
        LOGGER.debug("Pembayaran untuk tagihan {} berhasil disimpan", pt.getNomorTagihan());

    }

}
