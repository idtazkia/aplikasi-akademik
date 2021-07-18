package id.ac.tazkia.smilemahasiswa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import groovyjarjarasm.asm.commons.ModuleHashesAttribute;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.HapusTagihanResponse;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanResponse;
import id.ac.tazkia.smilemahasiswa.dto.payment.VaResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class KafkaListenerService {

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

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired private JenisTagihanDao jenisTagihanDao;

    @KafkaListener(topics = "${kafka.topic.tagihan.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(String message){
        try{
            PembayaranTagihan pt = objectMapper.readValue(message, PembayaranTagihan.class);
            log.info("Terima pembayaran tagihan {}", pt);
            Optional<JenisTagihan> optionalJenisTagihan = jenisTagihanDao.findById(pt.getJenisTagihan());

            if (!optionalJenisTagihan.isPresent()){
                log.debug("Bukan Tagihan Mahasiswa");
                return;
            }
            log.debug("Terima message : {}", message);

            Tagihan tagihan = tagihanDao.findByNomor(pt.getNomorTagihan());

            if (tagihan == null) {
                log.warn("Tagihan dengan nomor {} tidak ada di database", pt.getNomorTagihan());
                return;
            }

            tagihanService.prosesPembayaran(tagihan, pt);

            log.debug("jenis tagihan : {}",tagihan.getNilaiJenisTagihan().getJenisTagihan().getId());

        } catch (Exception err){
            log.warn(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.tagihan.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleTagihanResponse(String message){
        try{
            TagihanResponse response = objectMapper.readValue(message, TagihanResponse.class);
            Optional<JenisTagihan> optionalJenisTagihan = jenisTagihanDao.findById(response.getJenisTagihan());
            if (!optionalJenisTagihan.isPresent()){
                log.debug("Bukan tagihan mahasiswa");
                return;
            }
            log.debug("Terima message : {}", message);

            if (!response.getSukses()) {
                log.warn("Update tagihan gagal : {}", response.getDebitur());
                return;
            }

            log.debug("Update tagihan untuk mahasiswa {} sukses dengan nomor {}",
                    response.getDebitur(), response.getNomorTagihan());

            updateTagihan(response);

        }catch (Exception err){
            log.warn(err.getMessage(), err);
        }
    }

    @KafkaListener(topics = "${kafka.topic.va.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleVaResponse(String message){
        try{
            log.debug("Terima message : {}", message);
            VaResponse vaResponse = objectMapper.readValue(message, VaResponse.class);

            log.info("Memproses VA no {} di bank {} untuk tagihan {} ",
                    vaResponse.getAccountNumber(),
                    vaResponse.getBankId(),
                    vaResponse.getInvoiceNumber());
            insertNoVirtualAccount(vaResponse);
        }catch (IOException err){
            log.warn(err.getMessage(), err);
        }
    }

    private void updateTagihan(TagihanResponse tagihanResponse){
        log.info("Update tagihan nomor {} untuk mahasiswa {} ", tagihanResponse.getNomorTagihan(), tagihanResponse.getDebitur());
        Mahasiswa mahasiswa = mahasiswaDao.findByNim(tagihanResponse.getDebitur());
        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.PRAAKTIF);
        NilaiJenisTagihan nilaiJenisTagihan = nilaiJenisTagihanDao.
                findByJenisTagihanIdAndTahunAkademikAndProdiAndAngkatanAndProgramAndStatus(tagihanResponse.getJenisTagihan(), tahunAkademik, mahasiswa.getIdProdi(), mahasiswa.getAngkatan(),
                        mahasiswa.getIdProgram(), StatusRecord.AKTIF);
        if (nilaiJenisTagihan == null) {
            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            NilaiJenisTagihan nilaiTagihan = nilaiJenisTagihanDao.
                    findByJenisTagihanIdAndTahunAkademikAndProdiAndAngkatanAndProgramAndStatus(tagihanResponse.getJenisTagihan(), tahun, mahasiswa.getIdProdi(), mahasiswa.getAngkatan(),
                            mahasiswa.getIdProgram(), StatusRecord.AKTIF);
            Tagihan t =  tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahun, mahasiswa, nilaiTagihan, false);
            t.setNomor(tagihanResponse.getNomorTagihan());
            t.setKeterangan(tagihanResponse.getKeterangan());

            tagihanDao.save(t);
        }else{
            Tagihan tagihan = tagihanDao.findByStatusAndTahunAkademikAndMahasiswaAndNilaiJenisTagihanAndLunas(StatusRecord.AKTIF, tahunAkademik, mahasiswa, nilaiJenisTagihan, false);
            tagihan.setNomor(tagihanResponse.getNomorTagihan());
            tagihan.setKeterangan(tagihanResponse.getKeterangan());

            tagihanDao.save(tagihan);
        }

    }

    private void insertNoVirtualAccount(VaResponse vaResponse){
        Tagihan tagihan = tagihanDao.findByNomor(vaResponse.getInvoiceNumber());
        if (tagihan == null) {
            log.info("Tagihan dengan nomor {} tidak ada dalam database", vaResponse.getInvoiceNumber());
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
        log.info("Nomor VA {} di bank {} dengan nomor tagihan {} berhasil disimpan",
                vaResponse.getAccountNumber(), vaResponse.getBankId(),
                tagihan.getNomor());

    }


    @KafkaListener(topics = "${kafka.topic.hapus.tagihan.response}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleHapusTagihanResponse(String message) {
        try{
            HapusTagihanResponse response = objectMapper.readValue(message, HapusTagihanResponse.class);
            Optional<JenisTagihan> optionalJenisTagihan = jenisTagihanDao.findById(response.getJenisTagihan());
            if (!optionalJenisTagihan.isPresent()) {
                log.debug("Bukan Tagihan Mahasiswa");
                return;
            }

            log.debug("Terima message : {}", message);

            if (!response.getSukses()) {
                log.debug("Hapus tagihan gagal : {}", response.getDebitur());
                return;
            }

            if (response.getSukses()) {
                log.debug("Hapus tagihan sukses : {}", response.getDebitur());
//                kirimTagihanAfterDelete(response);
            }


        }catch (Exception err){
            log.warn(err.getMessage(), err);
        }
    }



}
