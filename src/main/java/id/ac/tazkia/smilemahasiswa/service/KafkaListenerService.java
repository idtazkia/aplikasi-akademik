package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanResponse;
import id.ac.tazkia.smilemahasiswa.dto.payment.VaResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.jfree.util.TableOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class KafkaListenerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);
    private static final List<String> JENIS_TAGIHAN_MAHASISWA = new ArrayList<>();

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private BankDao bankDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    @KafkaListener(topics = "${kafka.topic.tagihan.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(String message){
        try{
            PembayaranTagihan pt = objectMapper.readValue(message, PembayaranTagihan.class);
            if (!JENIS_TAGIHAN_MAHASISWA.contains(pt.getJenisTagihan())){
                LOGGER.debug("Bukan Tagihan Mahasiswa");
            }
            LOGGER.debug("Terima message : {}", message);

            Tagihan tagihan = tagihanDao.findByNomor(pt.getNomorTagihan());
            tagihanService.prosesPembayaran(tagihan, pt);

            System.out.println("jenis tagihan : " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getId());

        } catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.tagihan.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleTagihanResponse(String message){
        try{
            TagihanResponse response = objectMapper.readValue(message, TagihanResponse.class);
            if (!JENIS_TAGIHAN_MAHASISWA.contains(response.getJenisTagihan())){
                LOGGER.debug("Bukan tagihan mahasiswa");
                return;
            }
            LOGGER.debug("Terima message : {}", message);

            if (!response.getSukses()) {
                LOGGER.warn("Create tagihan gagal : {} ", response.getDebitur());
                return;
            }

            LOGGER.warn("Create tagihan untuk mahasiswa {} sukses dengan nomor {}",
                    response.getDebitur(), response.getNomorTagihan());

            insertTagihan(response);

        }catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.va.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handelVaResponse(String message){
        try{
            LOGGER.debug("Terima message : {}", message);
            VaResponse vaResponse = objectMapper.readValue(message, VaResponse.class);

            LOGGER.info("Memproses VA no {} di bank {} untuk tagihan {} ",
                    vaResponse.getAccountNumber(),
                    vaResponse.getBankId(),
                    vaResponse.getInvoiceNumber());
            insertNoVirtualAccount(vaResponse);
        }catch (IOException err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    private void insertTagihan(TagihanResponse tagihanResponse){
        LOGGER.debug("Insert tagihan nomor {} untuk mahasiswa {} ", tagihanResponse.getNomorTagihan(), tagihanResponse.getDebitur());

        Tagihan tagihan = new Tagihan();
        tagihan.setNomor(tagihanResponse.getNomorTagihan());
        tagihan.setMahasiswa(tagihanResponse.getMahasiswa());
        tagihan.setNilaiJenisTagihan(tagihanResponse.getJenisTagihan());
        tagihan.setKeterangan(tagihanResponse.getKeterangan());
        tagihan.setNilaiTagihan(tagihanResponse.getNilaiTagihan());
        tagihan.setTanggalPembuatan(tagihanResponse.getTanggalTagihan());
        tagihan.setTanggalJatuhTempo(tagihanResponse.getTanggalJatuhTempo());
        tagihan.setTanggalPenangguhan(LocalDate.now());
        tagihan.setTahunAkademik(tagihanResponse.getTahunAkademik());
        tagihan.setLunas(false);
        tagihan.setStatus(StatusRecord.AKTIF);
        tagihanDao.save(tagihan);

    }

    private void insertNoVirtualAccount(VaResponse vaResponse){
        Tagihan tagihan = tagihanDao.findByNomor(vaResponse.getInvoiceNumber());
        if (tagihan == null) {
            LOGGER.info("Tagihan dengan nomor {} tidak ada dalam database", vaResponse.getInvoiceNumber());
            return;
        }
        VirtualAccount virtualAccount = new VirtualAccount();
        virtualAccount.setTagihan(tagihan);

        Optional<Bank> b = bankDao.findById(vaResponse.getBankId());
        if (!b.isPresent()) {
            throw new IllegalStateException("Bank dengan id" + vaResponse.getBankId() + "tidak ada di database");
        }

        virtualAccount.setBank(b.get());
        virtualAccount.setNomor(vaResponse.getAccountNumber());

        virtualAccountDao.save(virtualAccount);
        LOGGER.info("Nomor VA {} di bank {} dengan nomor tagihan {} berhasil disimpan",
                vaResponse.getAccountNumber(), vaResponse.getBankId(),
                tagihan.getNomor());

    }

}
