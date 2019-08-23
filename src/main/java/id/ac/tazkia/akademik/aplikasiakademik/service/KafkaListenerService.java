package id.ac.tazkia.akademik.aplikasiakademik.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.akademik.aplikasiakademik.dao.KrsDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MahasiswaDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.TahunAkademikProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dto.PembayaranTagihan;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class KafkaListenerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaListenerService.class);
    private static final List<String> JENIS_TAGIHAN = new ArrayList<>();

    @Value("${tagihan.id.spp.tetap}") private String idSppTetap;
    @Value("#{'${tagihan.id.spp.variabel}'.split(',')}")
    private List<String> idSppVariable;

    @Autowired private MahasiswaDao mahasiswaDao;
    @Autowired private TahunAkademikDao tahunAkademikDao;
    @Autowired private TahunAkademikProdiDao tahunAkademikProdiDao;
    @Autowired private KrsDao krsDao;

    @PostConstruct
    public void inisialisai(){
        JENIS_TAGIHAN.add(idSppTetap);
        JENIS_TAGIHAN.addAll(idSppVariable);
    }

    @Autowired private ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.tagihan.payment}", groupId = "${spring.kafka.consumer.group-id}")
    public void handlePayment(String message) {
        try {
            PembayaranTagihan pt = objectMapper.readValue(message, PembayaranTagihan.class);
            if (!JENIS_TAGIHAN.contains(pt.getJenisTagihan())) {
                LOGGER.debug("Bukan Tagihan SPP");
                return;
            }
            LOGGER.debug("Terima message : {}", message);

            String nim = pt.getNomorDebitur();
            LOGGER.debug("Terima pembayaran untuk nim {} dengan jenis tagihan {} sebesar {}",
            nim, pt.getJenisTagihan(), pt.getNilaiPembayaran());

            Krs krs = new Krs();
            krs.setStatus(StatusRecord.AKTIF);
            krs.setTanggalTransaksi(LocalDateTime.now());

            // ambil data mahasiswa
            Mahasiswa m = mahasiswaDao.findByNim(nim);
            if (m == null) {
                LOGGER.error("Mahasiswa dengan nim {} tidak bisa ditemukan", nim);
                return;
            }
            krs.setMahasiswa(m);
            krs.setNim(nim);

            // data prodi
            krs.setProdi(m.getIdProdi());

            // tahun akademik
            TahunAkademik ta = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            if (ta == null) {
                LOGGER.error("Tahun akademik tidak bisa ditemukan");
            }
            krs.setTahunAkademik(ta);

            // tahun akademik prodi
            TahunAkademikProdi tap = tahunAkademikProdiDao.findByTahunAkademikAndProdi(ta, m.getIdProdi());
            if (tap == null) {
                LOGGER.error("Tahun akademik prodi tidak bisa ditemukan");
            }
            krs.setTahunAkademikProdi(tap);

            krsDao.save(krs);
            LOGGER.info("KRS untuk mahasiswa {}-{} sudah diaktifkan melalui pembayaran virtual akun dari bank {} senilai {}",
                    m.getNim(), m.getNama(), pt.getBank(), pt.getNilaiPembayaran());


        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
        }
    }




}
