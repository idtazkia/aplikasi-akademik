package id.ac.tazkia.akademik.aplikasiakademik.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JadwalDao;
import id.ac.tazkia.akademik.aplikasiakademik.dto.DataNotifikasiTerlambat;
import id.ac.tazkia.akademik.aplikasiakademik.dto.NotifikasiSmile;
import id.ac.tazkia.akademik.aplikasiakademik.entity.PresensiDosen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class NotifikasiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotifikasiService.class);

    @Value("${kafka.topic.notifikasi}") private String topicNotifikasi;
    @Value("${notifikasi.smile.konfigurasi.telat}") private String konfigurasiTelat;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JadwalDao jadwalDao;



    @Async
    public void kirimNotifikasiTelat(PresensiDosen presensiDosen){
        NotifikasiSmile notif = NotifikasiSmile.builder()
                .konfigurasi(konfigurasiTelat)
                .email(presensiDosen.getDosen().getKaryawan().getEmail())
                .mobile("")
                .data(DataNotifikasiTerlambat.builder()
                        .hariMasuk(presensiDosen.getJadwal().getHari().getNamaHari())
                        .hari(presensiDosen.getJadwal().getHari().getNamaHari())
                        .jam(presensiDosen.getJadwal().getJamMulai())
                        .jamMasuk(presensiDosen.getWaktuMasuk().toLocalTime())
                        .nama(presensiDosen.getDosen().getKaryawan().getNamaKaryawan())
                        .matakuliah(presensiDosen.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah())
                        .tanggalMasuk(presensiDosen.getWaktuMasuk().toLocalDate())
                        .build())
                .build();

        try {
            kafkaTemplate.send(topicNotifikasi, objectMapper.writeValueAsString(notif));
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
        }

        NotifikasiSmile notifikasiSmile = NotifikasiSmile.builder()
                .konfigurasi(konfigurasiTelat)
                .email("akademik@tazkia.ac.id")
                .mobile("")
                .data(DataNotifikasiTerlambat.builder()
                        .hariMasuk(presensiDosen.getJadwal().getHari().getNamaHari())
                        .hari(presensiDosen.getJadwal().getHari().getNamaHari())
                        .jam(presensiDosen.getJadwal().getJamMulai())
                        .jamMasuk(presensiDosen.getWaktuMasuk().toLocalTime())
                        .nama(presensiDosen.getDosen().getKaryawan().getNamaKaryawan())
                        .matakuliah(presensiDosen.getJadwal().getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah())
                        .tanggalMasuk(presensiDosen.getWaktuMasuk().toLocalDate())
                        .build())
                .build();

        try {
            kafkaTemplate.send(topicNotifikasi, objectMapper.writeValueAsString(notifikasiSmile));
        } catch (Exception err) {
            LOGGER.warn(err.getMessage(), err);
        }
    }


}
