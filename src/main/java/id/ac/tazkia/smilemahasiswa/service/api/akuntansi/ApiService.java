package id.ac.tazkia.smilemahasiswa.service.api.akuntansi;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.tazkia.smilemahasiswa.dao.PembayaranDao;
import id.ac.tazkia.smilemahasiswa.dao.RequestCicilanDao;
import id.ac.tazkia.smilemahasiswa.dao.TagihanDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.dto.api.akunting.*;
import id.ac.tazkia.smilemahasiswa.entity.*;
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
import java.util.Optional;

@Service
@Slf4j
public class ApiService {

    @Value("${akuntansi.baseurl}") private String baseAkuntansi;

    @Autowired private ObjectMapper objectMapper;

    @Autowired private TagihanDao tagihanDao;

    @Autowired private TahunAkademikDao tahunAkademikDao;

    @Autowired private PembayaranDao pembayaranDao;

    @Autowired private RequestCicilanDao requestCicilanDao;

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

    public boolean sendTagihan(SendTagihanDto tagihan) throws JsonProcessingException {

        String request = objectMapper.writeValueAsString(tagihan);
        log.debug("request: {}", request);

        try {
            SendTagihanDto resposne = webClient.post()
                    .uri(baseAkuntansi + "journal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SendTagihanDto.class)
                    .block();

            return resposne != null;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }
    }

    public boolean sendPembayaran(SendPembayaranDto pembayaran) throws JsonProcessingException{
        String request = objectMapper.writeValueAsString(pembayaran);
        log.debug("request: {}", request);
        try {
             SendPembayaranDto response = webClient.post()
                    .uri(baseAkuntansi + "journal-array")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(SendPembayaranDto.class)
                    .block();

             return response != null;
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
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
                    if (sendTagihan(tagihanDto)) {
                        t.setZahirExport(AkuntansiExport.SENT);
                        tagihanDao.save(t);
                    }else{
                        log.error("Gagal mengirim ke Akunting");
                    }
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
                    if (sendTagihan(tagihanDto)) {
                        t.setZahirExport(AkuntansiExport.DELETED);
                        tagihanDao.save(t);
                    }else{
                        log.error("Gagal mengupdate ke akunting");
                    }
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
                    if (p.getTagihan().getNilaiJenisTagihan().getKodePembayaranYys() != null && p.getTagihan().getNilaiJenisTagihan().getKodePembayaranInst() != null) {
                        SendPembayaranDto pembayaranDtoYys = new SendPembayaranDto();
                        pembayaranDtoYys.setCodeTemplate(p.getTagihan().getNilaiJenisTagihan().getKodePembayaranYys());
                        pembayaranDtoYys.setDateTransaction(String.valueOf(p.getWaktuBayar().toLocalDate()));
                        pembayaranDtoYys.setDescription("Pembayaran " + p.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama() + " Mahasiswa atas nama " + p.getTagihan().getMahasiswa().getNama() + " " + p.getTagihan().getMahasiswa().getNim());
                        if (p.getTipePengiriman() != null) {
                            pembayaranDtoYys.setAmounts(setPembayaranAkunting(p, "YYSN"));
                        }else{
                            ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                            if (p.getAmount().compareTo(BigDecimal.ZERO) == 0){
                                pembayaran.add(p.getAmount());
                                pembayaran.add(p.getAmount());
                            }else{
                                pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                                pembayaran.add(new BigDecimal(2000));
                                pembayaran.add(p.getAmount());
                            }
                            pembayaranDtoYys.setAmounts(pembayaran);
                        }
                        log.info("Kirim pembayaran yayasan : {}", pembayaranDtoYys);

                        SendPembayaranDto pembayaranDtoInst = new SendPembayaranDto();
                        pembayaranDtoInst.setCodeTemplate(p.getTagihan().getNilaiJenisTagihan().getKodePembayaranInst());
                        pembayaranDtoInst.setDateTransaction(String.valueOf(p.getWaktuBayar().toLocalDate()));
                        pembayaranDtoInst.setDescription("Pembayaran " + p.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama() + " Mahasiswa atas nama " + p.getTagihan().getMahasiswa().getNama() + " " + p.getTagihan().getMahasiswa().getNim());
                        if (p.getTipePengiriman() != null) {
                            pembayaranDtoInst.setAmounts(setPembayaranAkunting(p, "INST"));
                        }else{
                            ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                            pembayaran.add(p.getAmount());
                            pembayaran.add(p.getAmount());
                            pembayaranDtoInst.setAmounts(pembayaran);
                        }
                        log.info("Kirim pembayaran institut : {}", pembayaranDtoInst);

                        if (sendPembayaran(pembayaranDtoYys) && sendPembayaran(pembayaranDtoInst)) {
                            p.setZahirExport(AkuntansiExport.SENT);
                            pembayaranDao.save(p);
                            log.info("Pembayaran berhasil dikirim ke akunting");
                        }else{
                            log.error("Gagal mengirim ke akunting");
                        }
                    }else{
                        log.debug("Kode pembayaran tidak di temukan! {} {} {} {}", p.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama(), p.getTagihan().getNilaiJenisTagihan().getProdi().getNamaProdi(), p.getTagihan().getNilaiJenisTagihan().getProgram().getNamaProgram(), p.getTagihan().getNilaiJenisTagihan().getAngkatan());
                    }
                }
            }
        }
    }

    @Scheduled(cron = "${scheduled.sendCicilanTagihanAkun}", zone = "Asia/Jakarta")
    public void kirimTagihanCicilan() throws JsonProcessingException {
        log.info("Jalankan Scheduler kirim tagihan cicilan!");
        List<CicilanTagihanEmpatTahunDto> cekCicilanHP = tagihanDao.integrasiCicilanTagihanHP();
        if (!cekCicilanHP.isEmpty()) {
            for (CicilanTagihanEmpatTahunDto cicilan : cekCicilanHP){
                if (AkuntansiExport.NEW.toString().equals(cicilan.getZahirExport())) {
                    SendTagihanDto tagihanDto = new SendTagihanDto();
                    tagihanDto.setCodeTemplate(cicilan.getKodeJurnal());
                    tagihanDto.setDateTransaction(String.valueOf(cicilan.getTanggalJatuhTempo()));
                    tagihanDto.setDescription(cicilan.getKeterangan());
                    tagihanDto.setAmounts(cicilan.getNilaiCicilan());
                    log.info("Mengirim cicilan tagihan HP : {}", tagihanDto);
                    if (sendTagihan(tagihanDto)) {
                        Optional<Tagihan> cekTagihan = tagihanDao.findById(cicilan.getIdTagihan());
                        if (cekTagihan.isPresent()) {
                            Tagihan tagihan = cekTagihan.get();
                            tagihan.setZahirExport(AkuntansiExport.SENT);
                            tagihanDao.save(tagihan);
                        }
                        Optional<RequestCicilan> cekRequest = requestCicilanDao.findById(cicilan.getIdCicilan());
                        if (cekRequest.isPresent()) {
                            RequestCicilan requestCicilan = cekRequest.get();
                            requestCicilan.setZahirExport(AkuntansiExport.SENT);
                            requestCicilanDao.save(requestCicilan);
                        }
                    }else{
                        log.error("Gagal mengirim ke Akunting");
                    }
                }
            }
        }else{
            log.info("Tidak ada Data cekCicilanHP!");
        }
        List<CicilanTagihanEmpatTahunDto> cekCicilanNonHP = tagihanDao.integrasiCicilanTagihanNonHP();
        if (!cekCicilanNonHP.isEmpty()) {
            for (CicilanTagihanEmpatTahunDto cicilan : cekCicilanNonHP){
                if (AkuntansiExport.NEW.toString().equals(cicilan.getZahirExport())) {
                    SendTagihanDto tagihanDto = new SendTagihanDto();
                    tagihanDto.setCodeTemplate(cicilan.getKodeJurnal());
                    tagihanDto.setDateTransaction(String.valueOf(cicilan.getTanggalJatuhTempo()));
                    tagihanDto.setDescription(cicilan.getKeterangan());
                    tagihanDto.setAmounts(cicilan.getNilaiCicilan());
                    log.info("Mengirim cicilan tagihan NON-HP : {}", tagihanDto);
                    if (sendTagihan(tagihanDto)) {
                        Optional<Tagihan> cekTagihan = tagihanDao.findById(cicilan.getIdTagihan());
                        if (cekTagihan.isPresent()) {
                            Tagihan tagihan = cekTagihan.get();
                            tagihan.setZahirExport(AkuntansiExport.SENT);
                            tagihanDao.save(tagihan);
                        }
                        Optional<RequestCicilan> cekRequest = requestCicilanDao.findById(cicilan.getIdCicilan());
                        if (cekRequest.isPresent()) {
                            RequestCicilan requestCicilan = cekRequest.get();
                            requestCicilan.setZahirExport(AkuntansiExport.SENT);
                            requestCicilanDao.save(requestCicilan);
                        }
                    }else{
                        log.error("Gagal mengirim ke Akunting");
                    }
                }
            }
        }else{
            log.info("Tidak ada Data cekCicilanNonHP Kosong!");
        }
    }

    @Scheduled(cron = "${scheduled.sendCicilanPembayaranAkun}", zone = "Asia/Jakarta")
    public void kirimPembayaraCicilan() throws JsonProcessingException {
        List<CicilanPembayaranEmpatTahunDto> cekPembayaranCicilanHP = pembayaranDao.integrasiCicilanPembayaranHP();
        if (!cekPembayaranCicilanHP.isEmpty()) {
            for (CicilanPembayaranEmpatTahunDto cicilanPembayaran : cekPembayaranCicilanHP){
                if (AkuntansiExport.NEW.equals(cicilanPembayaran.getZahirExport())){
                    SendPembayaranDto pembayaranDtoYys = new SendPembayaranDto();
                    pembayaranDtoYys.setCodeTemplate("YYSN-KAS-MHS");
                    pembayaranDtoYys.setDateTransaction(String.valueOf(cicilanPembayaran.getWaktuBayar().toLocalDate()));
                    pembayaranDtoYys.setDescription(cicilanPembayaran.getKeterangan());
                    if (cicilanPembayaran.getTipePengiriman() != null) {
                        pembayaranDtoYys.setAmounts(setPembayaranCicilanAkunting(cicilanPembayaran, "YYSN"));
                    }else{
                        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                        if (cicilanPembayaran.getNilaiCicilan().compareTo(BigDecimal.ZERO) == 0){
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        }else{
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan().subtract(new BigDecimal(500)));
                            pembayaran.add(new BigDecimal(500));
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        }
                        pembayaranDtoYys.setAmounts(pembayaran);
                    }
                    log.info("Kirim pembayaran yayasan : {}", pembayaranDtoYys);

                    SendPembayaranDto pembayaranDtoInst = new SendPembayaranDto();
                    pembayaranDtoInst.setCodeTemplate(cicilanPembayaran.getKodeJurnal());
                    pembayaranDtoInst.setDateTransaction(String.valueOf(cicilanPembayaran.getWaktuBayar().toLocalDate()));
                    pembayaranDtoInst.setDescription(cicilanPembayaran.getKeterangan());
                    if (cicilanPembayaran.getTipePengiriman() != null) {
                        pembayaranDtoInst.setAmounts(setPembayaranCicilanAkunting(cicilanPembayaran, "INST"));
                    }else{
                        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                        pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        pembayaranDtoInst.setAmounts(pembayaran);
                    }
                    log.info("Kirim pembayaran institut : {}", pembayaranDtoInst);

                    if (sendPembayaran(pembayaranDtoYys) && sendPembayaran(pembayaranDtoInst)) {
                        Optional<Pembayaran> cekPembayaran = pembayaranDao.findById(cicilanPembayaran.getId());
                        if (cekPembayaran.isPresent()) {
                            Pembayaran p = cekPembayaran.get();
                            p.setZahirExport(AkuntansiExport.SENT);
                            pembayaranDao.save(p);
                        }
                        log.info("Pembayaran berhasil dikirim ke akunting");
                    }else{
                        log.error("Gagal mengirim ke akunting");
                    }
                }
            }
        }
        List<CicilanPembayaranEmpatTahunDto> cekPembayaranCicilanNonHP = pembayaranDao.integrasiCicilanPembayaranNonHP();
        if (!cekPembayaranCicilanNonHP.isEmpty()) {
            for (CicilanPembayaranEmpatTahunDto cicilanPembayaran : cekPembayaranCicilanNonHP){
                if (AkuntansiExport.NEW.equals(cicilanPembayaran.getZahirExport())){
                    SendPembayaranDto pembayaranDtoYys = new SendPembayaranDto();
                    pembayaranDtoYys.setCodeTemplate(cicilanPembayaran.getKodeJurnal());
                    pembayaranDtoYys.setDateTransaction(String.valueOf(cicilanPembayaran.getWaktuBayar().toLocalDate()));
                    pembayaranDtoYys.setDescription(cicilanPembayaran.getKeterangan());
                    if (cicilanPembayaran.getTipePengiriman() != null) {
                        pembayaranDtoYys.setAmounts(setPembayaranCicilanAkunting(cicilanPembayaran, "YYSN"));
                    }else{
                        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                        if (cicilanPembayaran.getNilaiCicilan().compareTo(BigDecimal.ZERO) == 0){
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        }else{
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan().subtract(new BigDecimal(500)));
                            pembayaran.add(new BigDecimal(500));
                            pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        }
                        pembayaranDtoYys.setAmounts(pembayaran);
                    }
                    log.info("Kirim pembayaran yayasan : {}", pembayaranDtoYys);

                    SendPembayaranDto pembayaranDtoInst = new SendPembayaranDto();
                    pembayaranDtoInst.setCodeTemplate("YYSN-KAS-MHS");
                    pembayaranDtoInst.setDateTransaction(String.valueOf(cicilanPembayaran.getWaktuBayar().toLocalDate()));
                    pembayaranDtoInst.setDescription(cicilanPembayaran.getKeterangan());
                    if (cicilanPembayaran.getTipePengiriman() != null) {
                        pembayaranDtoInst.setAmounts(setPembayaranCicilanAkunting(cicilanPembayaran, "INST"));
                    }else{
                        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
                        pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        pembayaran.add(cicilanPembayaran.getNilaiCicilan());
                        pembayaranDtoInst.setAmounts(pembayaran);
                    }
                    log.info("Kirim pembayaran institut : {}", pembayaranDtoInst);

                    if (sendPembayaran(pembayaranDtoYys) && sendPembayaran(pembayaranDtoInst)) {
                        Optional<Pembayaran> cekPembayaran = pembayaranDao.findById(cicilanPembayaran.getId());
                        if (cekPembayaran.isPresent()) {
                            Pembayaran p = cekPembayaran.get();
                            p.setZahirExport(AkuntansiExport.SENT);
                            pembayaranDao.save(p);
                        }
                        log.info("Pembayaran berhasil dikirim ke akunting");
                    }else{
                        log.error("Gagal mengirim ke akunting");
                    }
                }
            }
        }
    }

    private ArrayList<BigDecimal> setPembayaranAkunting(Pembayaran p, String untuk){
        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
        if ("YYSN".equalsIgnoreCase(untuk)) {
            switch (p.getTipePengiriman()){
                case MANUAL:
                    pembayaran.add(p.getAmount());
                    pembayaran.add(BigDecimal.ZERO);
                    pembayaran.add(p.getAmount());
                    break;
                case VIRTUAL_ACCOUNT:
                    pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                    pembayaran.add(new BigDecimal(2000));
                    pembayaran.add(p.getAmount());
                    break;
                default:
                    pembayaran.add(p.getAmount().subtract(new BigDecimal(2000)));
                    pembayaran.add(new BigDecimal(2000));
                    pembayaran.add(p.getAmount());
                    break;
            }
            log.info("Kirim tagihan yayasan : {}", pembayaran);
        }else{
            pembayaran.add(p.getAmount());
            pembayaran.add(p.getAmount());
            log.info("Kirim tagihan institut : {}", pembayaran);
        }
        return pembayaran;
    }

    private ArrayList<BigDecimal> setPembayaranCicilanAkunting(CicilanPembayaranEmpatTahunDto p, String untuk){
        ArrayList<BigDecimal> pembayaran = new ArrayList<>();
        if ("YYSN".equalsIgnoreCase(untuk)) {
            switch (p.getTipePengiriman()){
                case MANUAL:
                    pembayaran.add(p.getNilaiCicilan());
                    pembayaran.add(BigDecimal.ZERO);
                    pembayaran.add(p.getNilaiCicilan());
                    break;
                case VIRTUAL_ACCOUNT:
                    pembayaran.add(p.getNilaiCicilan().subtract(new BigDecimal(500)));
                    pembayaran.add(new BigDecimal(500));
                    pembayaran.add(p.getNilaiCicilan());
                    break;
                default:
                    pembayaran.add(p.getNilaiCicilan().subtract(new BigDecimal(500)));
                    pembayaran.add(new BigDecimal(500));
                    pembayaran.add(p.getNilaiCicilan());
                    break;
            }
            log.info("Kirim tagihan yayasan : {}", pembayaran);
        }else{
            pembayaran.add(p.getNilaiCicilan());
            pembayaran.add(p.getNilaiCicilan());
            log.info("Kirim tagihan institut : {}", pembayaran);
        }
        return pembayaran;
    }

}