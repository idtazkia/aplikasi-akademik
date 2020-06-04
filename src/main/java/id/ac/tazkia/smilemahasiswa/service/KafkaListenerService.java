package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dao.PembayaranDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.entity.Bank;
import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import org.jfree.util.TableOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class KafkaListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);
    private static final List<String> JENIS_TAGIHAN_MAHASISWA = new ArrayList<>();
    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private PembayaranDao pembayaranDao;

    @KafkaListener(topics = "${kafka.topic.tagihan.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(String message){
        try{
            PembayaranTagihan pt = objectMapper.readValue(message, PembayaranTagihan.class);
            if (!JENIS_TAGIHAN_MAHASISWA.contains(pt.getJenisTagihan())){
                LOGGER.debug("Bukan Tagihan Mahasiswa");
            }
            LOGGER.debug("Terima message : {}", message);

            Tagihan tagihan = tagihanDao.findByNomor(pt.getNomorTagihan());
            prosesPembayaran(tagihan, pt);

            System.out.println("jenis tagihan : " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getId());

        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

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
