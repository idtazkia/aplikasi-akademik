package id.ac.tazkia.smilemahasiswa.service.api.akuntansi;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dao.PembayaranDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.SendPembayaranDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.SendTagihanDto;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.TemplateDto;
import id.ac.tazkia.smilemahasiswa.entity.AkuntansiExport;
import id.ac.tazkia.smilemahasiswa.entity.Pembayaran;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.Tagihan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class ApiService {

    @Value("${akuntansi.baseurl}") private String baseAkuntansi;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private TagihanDao tagihanDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Autowired private PembayaranDao pembayaranDao;

    WebClient webClient = WebClient.builder()
            .baseUrl(baseAkuntansi)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    public TemplateDto getTemplate(){
        return webClient.get()
                .uri(baseAkuntansi + "journal-template?type=SMILE")
                .retrieve()
                .bodyToMono(TemplateDto.class)
                .block();
    }

    public void sendTagihan(SendTagihanDto tagihan) throws JsonProcessingException {

        String request = objectMapper.writeValueAsString(tagihan);
        log.debug("request: {}", request);

        try {
            webClient.post()
                    .uri(baseAkuntansi + "journal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SendTagihanDto.class)
                    .block();
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    public void sendPembayaran(SendPembayaranDto pembayaran) throws JsonProcessingException{
        String request = objectMapper.writeValueAsString(pembayaran);
        log.debug("request: {}", request);
        try {
            webClient.post()
                    .uri(baseAkuntansi + "journal-array")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SendPembayaranDto.class)
                    .block();
        }catch (Exception e){
            log.error(e.getMessage());
        }
    }

    @Scheduled(cron = "${scheduled.sendTagihanAkun}", zone = "Asia/Jakarta")
    public void kirimTagihan() throws JsonProcessingException {
        List<Tagihan> cekTagihan = tagihanDao.findByStatusAndZahirExport(StatusRecord.AKTIF, AkuntansiExport.NEW);
        if(!cekTagihan.isEmpty()){
            for (Tagihan t : cekTagihan){
                if (t.getNilaiJenisTagihan().getKodeTagihan() != null) {
                    SendTagihanDto tagihanDto = new SendTagihanDto();
                    tagihanDto.setCodeTemplate(t.getNilaiJenisTagihan().getKodeTagihan());
                    tagihanDto.setDateTransaction(String.valueOf(t.getTanggalPembuatan()));
                    tagihanDto.setDescription("Tagihan " + t.getNilaiJenisTagihan().getJenisTagihan().getNama() + " a.n " + t.getMahasiswa().getNama() + " " + t.getMahasiswa().getNim());
                    tagihanDto.setAmounts(t.getNilaiTagihanAsli());
                    sendTagihan(tagihanDto);

                    t.setZahirExport(AkuntansiExport.SENT);
                    tagihanDao.save(t);
                }else{
                    log.debug("Kode tagihan tidak di temukan! {}, {}, {}, {}", t.getNilaiJenisTagihan().getJenisTagihan().getNama(), t.getNilaiJenisTagihan().getProdi().getNamaProdi(), t.getNilaiJenisTagihan().getProgram().getNamaProgram(), t.getNilaiJenisTagihan().getAngkatan());
                }
            }
        }
    }

    @Scheduled(cron = "${scheduled.sendTagihanHapus}", zone = "Asia/Jakarta")
    public void kirimTagihandiHapus() throws JsonProcessingException {
        List<Tagihan> cekTagihan = tagihanDao.findByStatusAndZahirExport(StatusRecord.HAPUS, AkuntansiExport.SENT);
        if(!cekTagihan.isEmpty()){
            for (Tagihan t : cekTagihan){
                if (t.getNilaiJenisTagihan().getKodeTagihan() != null) {
                    SendTagihanDto tagihanDto = new SendTagihanDto();
                    tagihanDto.setCodeTemplate(t.getNilaiJenisTagihan().getKodeTagihan());
                    tagihanDto.setDateTransaction(String.valueOf(t.getTanggalPembuatan()));
                    tagihanDto.setDescription("Pembalik tagihan " + t.getNilaiJenisTagihan().getJenisTagihan().getNama() + " a.n " + t.getMahasiswa().getNama() + " " + t.getMahasiswa().getNim());
                    tagihanDto.setType("reverse");
                    tagihanDto.setAmounts(t.getNilaiTagihanAsli());
                    sendTagihan(tagihanDto);

                    t.setZahirExport(AkuntansiExport.DELETED);
                    tagihanDao.save(t);
                }else{
                    log.debug("Kode tagihan tidak di temukan! {}, {}, {}, {}", t.getNilaiJenisTagihan().getJenisTagihan().getNama(), t.getNilaiJenisTagihan().getProdi().getNamaProdi(), t.getNilaiJenisTagihan().getProgram().getNamaProgram(), t.getNilaiJenisTagihan().getAngkatan());
                }
            }
        }
    }

    @Scheduled(cron = "${scheduled.sendPembayaranAkun}", zone = "Asia/Jakarta")
    public void kirimPembayaran() throws JsonProcessingException {
        List<Pembayaran> cekPembayaran = pembayaranDao.findByStatusAndZahirExport(StatusRecord.AKTIF, AkuntansiExport.NEW);
        if(!cekPembayaran.isEmpty()){
            for (Pembayaran p : cekPembayaran){
                if (p.getTagihan().getZahirExport() == AkuntansiExport.SENT){
                    if (p.getTagihan().getNilaiJenisTagihan().getKodePembayaran() != null) {
                        SendPembayaranDto pembayaranDto = new SendPembayaranDto();
                        pembayaranDto.setCodeTemplate(p.getTagihan().getNilaiJenisTagihan().getKodePembayaran());
                        pembayaranDto.setDateTransaction(String.valueOf(p.getWaktuBayar().toLocalDate()));
                        pembayaranDto.setDescription("Pembayaran Mahasiswa atas nama " + p.getTagihan().getMahasiswa().getNama() + " " + p.getTagihan().getMahasiswa().getNim());
                        if (p.getTipePengiriman() != null) {
                            ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                            switch (p.getTipePengiriman()){
                                case MANUAL:
                                    pembayaran.add(p.getAmount());
                                    pembayaran.add(p.getAmount());
                                    pembayaran.add(BigDecimal.ZERO);
                                    pembayaranDto.setAmounts(pembayaran);
                                    break;
                                case VIRTUAL_ACCOUNT:
                                    pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                                    pembayaran.add(p.getAmount());
                                    pembayaran.add(new BigDecimal(2000));
                                    pembayaranDto.setAmounts(pembayaran);
                                    break;
                                default:
                                    pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                                    pembayaran.add(p.getAmount());
                                    pembayaran.add(new BigDecimal(2000));
                                    pembayaranDto.setAmounts(pembayaran);
                                    break;
                            }
                        }else{
                            ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                            pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                            pembayaran.add(p.getAmount());
                            pembayaran.add(new BigDecimal(2000));
                            pembayaranDto.setAmounts(pembayaran);
                        }
                        sendPembayaran(pembayaranDto);

                        p.setZahirExport(AkuntansiExport.SENT);
                        pembayaranDao.save(p);
                    }else{
                        log.debug("Kode pembayaran tidak di temukan! {} {} {} {}", p.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama(), p.getTagihan().getNilaiJenisTagihan().getProdi().getNamaProdi(), p.getTagihan().getNilaiJenisTagihan().getProgram().getNamaProgram(), p.getTagihan().getNilaiJenisTagihan().getAngkatan());
                    }
                }
            }
        }
    }
}