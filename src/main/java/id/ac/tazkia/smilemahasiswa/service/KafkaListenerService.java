package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanResponse;
import id.ac.tazkia.smilemahasiswa.dto.payment.VaResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import org.apache.kafka.common.metrics.Stat;
import org.jfree.util.TableOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.swing.text.html.Option;
import javax.validation.Valid;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagihanService tagihanService;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    private BankDao bankDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private VirtualAccountDao virtualAccountDao;

    @Autowired private JenisTagihanDao jenisTagihanDao;

    @KafkaListener(topics = "${kafka.topic.tagihan.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(String message){
        try{
            PembayaranTagihan pt = objectMapper.readValue(message, PembayaranTagihan.class);
            Optional<JenisTagihan> optionalJenisTagihan = jenisTagihanDao.findById(pt.getJenisTagihan());

            if (!optionalJenisTagihan.isPresent()){
                LOGGER.debug("Bukan Tagihan Mahasiswa");
                return;
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
            Optional<JenisTagihan> optionalJenisTagihan = jenisTagihanDao.findById(response.getJenisTagihan());
            if (!optionalJenisTagihan.isPresent()){
                LOGGER.debug("Bukan tagihan mahasiswa");
                return;
            }
            LOGGER.debug("Terima message : {}", message);

            if (!response.getSukses()) {
                LOGGER.warn("Create tagihan gagal : {}", response.getDebitur());
                return;
            }

            LOGGER.debug("Create tagihan untuk mahasiswa {} sukses dengan nomor {}",
                    response.getDebitur(), response.getNomorTagihan());

            insertTagihan(response);

        }catch (Exception err){
            LOGGER.warn(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.va.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleVaResponse(String message){
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
        LOGGER.debug("Update tagihan nomor {} untuk mahasiswa {} ", tagihanResponse.getNomorTagihan(), tagihanResponse.getDebitur());
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(tagihanResponse.getDebitur());
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.findByJenisTagihanIdAndTahunAkademikAndProdiAndAngkatanAndStatus(tagihanResponse.getJenisTagihan(), tahunAkademik, mahasiswa.getIdProdi(), mahasiswa.getAngkatan(), StatusRecord.AKTIF);
        Tagihan tagihan = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihan(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan);
//        Tagihan tagihan = new Tagihan();
        tagihan.setNomor(tagihanResponse.getNomorTagihan());
        tagihan.setKeterangan(tagihanResponse.getKeterangan());

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
