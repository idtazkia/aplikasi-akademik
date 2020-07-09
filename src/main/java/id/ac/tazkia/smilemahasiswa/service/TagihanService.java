package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import id.ac.tazkia.smilemahasiswa.dao.PembayaranDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
public class TagihanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TagihanService.class);
    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private TagihanDao tagihanDao;

    public void prosesPembayaran(Tagihan tagihan, PembayaranTagihan pt){
        tagihan.setLunas(true);

        System.out.println("Pembayaran Tagihan = " + pt);

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTagihan(tagihan);
        pembayaran.setAmount(pt.getNilaiPembayaran());
        pembayaran.setWaktuBayar(LocalDate.parse(pt.getWaktuPembayaran(), FORMATTER_ISO_DATE_TIME));
        pembayaran.setReferensi(pt.getReferensiPembayaran());

        Bank bank = new Bank();
        bank.setId(pt.getBank());
        pembayaran.setBank(bank);

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);

    }

}
