package id.ac.tazkia.smilemahasiswa.service;

import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.payment.HapusTagihanRequest;
import id.ac.tazkia.smilemahasiswa.dto.payment.PembayaranTagihan;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanRequest;
import id.ac.tazkia.smilemahasiswa.dto.payment.TagihanResponse;
import id.ac.tazkia.smilemahasiswa.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.tomcat.jni.Local;
import org.apache.tomcat.jni.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;


import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
@Slf4j
public class TagihanService {

    private static final DateTimeFormatter FORMATTER_ISO_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String TAGIHAN_SEMPRO = "08";
    public static final String TAGIHAN_SKRIPSI = "09";
    public static final String TAGIHAN_UTS = "12";
    public static final String TAGIHAN_UAS = "13";
    public static final String TAGIHAN_SP = "23";
    public static final List<String> TAGIHAN_KRS = Arrays.asList("14", "22", "40", "44");

    public static final String TAGIHAN_TAHFIDZ = "16";

    public static final String TAGIHAN_KOMPRE = "17";

    @Autowired
    private PembayaranDao pembayaranDao;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private TagihanDao tagihanDao;

    @Autowired
    private TahunProdiDao tahunProdiDao;

    @Autowired
    private EnableFitureDao enableFitureDao;

    @Autowired
    private KrsDao krsDao;

    @Autowired
    private RequestCicilanDao requestCicilanDao;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PraKrsSpDao praKrsSpDao;

    @Autowired
    private NilaiJenisTagihanDao nilaiJenisTagihanDao;

    @Autowired
    private JenisTagihanDao jenisTagihanDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private MahasiswaCicilanDao cicilanDao;

    @Autowired
    private RequestPenangguhanDao requestPenangguhanDao;

    @Autowired
    private TagihanBeasiswaDao tagihanBeasiswaDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private KelasDao kelasDao;

    public void generateTagihan(List<Mahasiswa> reguler, List<Mahasiswa> beasiswa, TahunAkademik tahun, String angkatan, TahunAkademik tahunBefore){

        if (reguler != null){
            for (Mahasiswa mhs : reguler){
                MahasiswaCicilan mcicilan = cicilanDao.findByStatusAndMahasiswa(StatusRecord.AKTIF, mhs);
                if (mcicilan == null){
                    List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, angkatan, mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
                    for (NilaiJenisTagihan njt : nilaiJenisTagihans) {
                        Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                        if (tCekSudahAda == null) {
                            Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                            if (tagihan1 == null) {
                                String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                        + " a.n. " + mhs.getNama();

                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mhs);
                                tagihan.setNilaiJenisTagihan(njt);
                                tagihan.setKeterangan(keteranganTagihan);
                                tagihan.setNilaiTagihan(njt.getNilai());
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahun);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihanDao.save(tagihan);
                                requestCreateTagihan(tagihan);

                                EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                if (enableFiture == null) {
                                    createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                }

                            } else {

                                tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                tagihan1.setStatus(StatusRecord.NONAKTIF);
                                tagihanDao.save(tagihan1);
                                hapusTagihan(tagihan1);

                                Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                Tagihan tagihan = new Tagihan();
                                tagihan.setMahasiswa(mhs);
                                tagihan.setNilaiJenisTagihan(njt);
                                tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                        + " a.n. " + tagihan.getMahasiswa().getNama());
                                tagihan.setNilaiTagihan(njt.getNilai().add(new BigDecimal(sisaCicilan)));
                                tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                tagihan.setTanggalPembuatan(LocalDate.now());
                                tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                tagihan.setTahunAkademik(tahun);
                                tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                tagihan.setStatus(StatusRecord.AKTIF);
                                tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                tagihanDao.save(tagihan);
                                requestCreateTagihan(tagihan);

                                EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                if (enableFiture == null) {
                                    createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                }

                                if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                    List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                    if (cekSisaCicilan != null) {
                                        for (RequestCicilan listCicilan : cekSisaCicilan){
                                            listCicilan.setStatus(StatusRecord.HAPUS);
                                            listCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                                            listCicilan.setStatusApprove(StatusApprove.HAPUS);
                                            requestCicilanDao.save(listCicilan);
                                        }
                                    }
                                }

                                if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                    RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                    if (rp != null) {
                                        rp.setStatus(StatusRecord.HAPUS);
                                        rp.setStatusApprove(StatusApprove.HAPUS);
                                        requestPenangguhanDao.save(rp);
                                    }
                                }

                            }
                        }else{
                            log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                        }
                    }
                }else{
                    EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                    if (ef == null) {
                        EnableFiture enableFiture = new EnableFiture();
                        enableFiture.setMahasiswa(mcicilan.getMahasiswa());
                        enableFiture.setTahunAkademik(tahun);
                        enableFiture.setFitur(StatusRecord.KRS);
                        enableFiture.setEnable(true);
                        enableFiture.setKeterangan("-");

                        enableFitureDao.save(enableFiture);
                    }
                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                    if (k == null) {
                        Krs krs = new Krs();
                        krs.setTahunAkademik(tahun);
                        krs.setTahunAkademikProdi(tahunProdi);
                        krs.setProdi(mcicilan.getMahasiswa().getIdProdi());
                        krs.setMahasiswa(mcicilan.getMahasiswa());
                        krs.setNim(mcicilan.getMahasiswa().getNim());
                        krs.setTanggalTransaksi(LocalDateTime.now());
                        krs.setStatus(StatusRecord.AKTIF);
                        krsDao.save(krs);
                    }
                }
            }
        }

        if (beasiswa != null){
            for(Mahasiswa mhs : beasiswa){
                MahasiswaCicilan mcicilan = cicilanDao.findByStatusAndMahasiswa(StatusRecord.AKTIF, mhs);
                if (mcicilan == null){
                    List<NilaiJenisTagihan> nilaiJenisTagihans = nilaiJenisTagihanDao.findByTahunAkademikAndAngkatanAndProdiAndProgramAndStatus(tahun, mhs.getAngkatan(), mhs.getIdProdi(), mhs.getIdProgram(), StatusRecord.AKTIF);
                    for (NilaiJenisTagihan njt : nilaiJenisTagihans){
                        TagihanBeasiswa tBeasiswa = tagihanBeasiswaDao.findByBeasiswaAndJenisTagihanAndStatus(mhs.getBeasiswa(), njt.getJenisTagihan(), StatusRecord.AKTIF);
                        if (tBeasiswa == null){
                            Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                            if (tCekSudahAda == null) {
                                Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                                if (tagihan1 == null) {
                                    String keteranganTagihan = mhs.getBeasiswa().getNamaBeasiswa();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan(keteranganTagihan);
                                    tagihan.setNilaiTagihan(BigDecimal.ZERO);
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now());
                                    tagihan.setTanggalPenangguhan(LocalDate.now());
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setLunas(true);
                                    tagihan.setStatusTagihan(StatusTagihan.LUNAS);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihanDao.save(tagihan);

                                    Pembayaran pembayaran = new Pembayaran();
                                    pembayaran.setTagihan(tagihan);
                                    pembayaran.setAmount(tagihan.getNilaiTagihan());
                                    pembayaran.setWaktuBayar(LocalDateTime.now());
                                    pembayaran.setNomorRekening("-");
                                    pembayaran.setReferensi("-");
                                    pembayaran.setStatus(StatusRecord.AKTIF);
                                    pembayaranDao.save(pembayaran);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, true);
                                    }

                                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());

                                    Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                                    if (krs == null) {
                                        createKrs(tagihan, tahunProdi);
                                    }

                                } else {

                                    tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                    tagihan1.setStatus(StatusRecord.NONAKTIF);
                                    tagihanDao.save(tagihan1);
                                    hapusTagihan(tagihan1);

                                    Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                            + " a.n. " + tagihan.getMahasiswa().getNama());
                                    tagihan.setNilaiTagihan(new BigDecimal(sisaCicilan));
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                    tagihanDao.save(tagihan);
                                    requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                        if (cekSisaCicilan != null) {
                                            for (RequestCicilan listCicilan : cekSisaCicilan){
                                                listCicilan.setStatus(StatusRecord.HAPUS);
                                                listCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                                                listCicilan.setStatusApprove(StatusApprove.HAPUS);
                                                requestCicilanDao.save(listCicilan);
                                            }
                                        }
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                        RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                        if (rp != null) {
                                            rp.setStatus(StatusRecord.HAPUS);
                                            rp.setStatusApprove(StatusApprove.HAPUS);
                                            requestPenangguhanDao.save(rp);
                                        }
                                    }

                                }
                            }else{
                                log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                            }

                        }else {
                            String jenis = tBeasiswa.getJenisPotongan();
                            Integer total = null;
                            Integer potongan = tBeasiswa.getPotongan();
                            if (jenis.equals("PERSEN")){
                                Integer totalPotongan = njt.getNilai().intValue() * potongan / 100;
                                total = njt.getNilai().intValue() - totalPotongan;
                            }else if (jenis.equals("JUMLAH")){
                                total = njt.getNilai().intValue() - potongan;
                            }

                            Tagihan tCekSudahAda = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndStatus(mhs, njt.getJenisTagihan(), tahun, StatusRecord.AKTIF);
                            if (tCekSudahAda == null) {
                                Tagihan tagihan1 = tagihanDao.findByMahasiswaAndNilaiJenisTagihanJenisTagihanAndTahunAkademikAndLunasAndStatus(mhs, njt.getJenisTagihan(), tahunBefore, false, StatusRecord.AKTIF);
                                if (tagihan1 == null) {

                                    String keteranganTagihan = "Tagihan " + njt.getJenisTagihan().getNama()
                                            + " a.n. " + mhs.getNama();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan(keteranganTagihan);
                                    tagihan.setNilaiTagihan(new BigDecimal(total));
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihanDao.save(tagihan);
                                    requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                } else {

                                    tagihan1.setStatusTagihan(StatusTagihan.NONAKTIF);
                                    tagihan1.setStatus(StatusRecord.NONAKTIF);
                                    tagihanDao.save(tagihan1);
                                    hapusTagihan(tagihan1);

                                    Integer sisaCicilan = tagihan1.getNilaiTagihan().intValue() - tagihan1.getAkumulasiPembayaran().intValue();

                                    Tagihan tagihan = new Tagihan();
                                    tagihan.setMahasiswa(mhs);
                                    tagihan.setNilaiJenisTagihan(njt);
                                    tagihan.setKeterangan("Tagihan " + tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                                            + " a.n. " + tagihan.getMahasiswa().getNama());
                                    tagihan.setNilaiTagihan(new BigDecimal(total).add(new BigDecimal(sisaCicilan)));
                                    tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
                                    tagihan.setTanggalPembuatan(LocalDate.now());
                                    tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
                                    tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
                                    tagihan.setTahunAkademik(tahun);
                                    tagihan.setStatusTagihan(StatusTagihan.AKTIF);
                                    tagihan.setStatus(StatusRecord.AKTIF);
                                    tagihan.setIdTagihanSebelumnya(tagihan1.getId());
                                    tagihanDao.save(tagihan);
                                    requestCreateTagihan(tagihan);

                                    EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, false, tahun);
                                    if (enableFiture == null) {
                                        createEnableFitur(tahun, tagihan, StatusRecord.KRS, false);
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DICICIL) {
                                        List<RequestCicilan> cekSisaCicilan = requestCicilanDao.findByTagihanAndStatusAndStatusCicilanNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusCicilan.LUNAS));
                                        if (cekSisaCicilan != null) {
                                            for (RequestCicilan listCicilan : cekSisaCicilan){
                                                listCicilan.setStatus(StatusRecord.HAPUS);
                                                listCicilan.setStatusCicilan(StatusCicilan.BATAL_CICIL);
                                                listCicilan.setStatusApprove(StatusApprove.HAPUS);
                                                requestCicilanDao.save(listCicilan);
                                            }
                                        }
                                    }

                                    if (tagihan1.getStatusTagihan() == StatusTagihan.DITANGGUHKAN) {
                                        RequestPenangguhan rp = requestPenangguhanDao.findByTagihanAndStatusAndStatusApproveNotIn(tagihan1, StatusRecord.AKTIF, Arrays.asList(StatusApprove.WAITING));
                                        if (rp != null) {
                                            rp.setStatus(StatusRecord.HAPUS);
                                            rp.setStatusApprove(StatusApprove.HAPUS);
                                            requestPenangguhanDao.save(rp);
                                        }
                                    }

                                }
                            }else{
                                log.info("Tagihan {} sudah ada!", tCekSudahAda.getMahasiswa().getNim());
                            }
                        }
                    }
                }else{
                    EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(mhs, StatusRecord.KRS, true, tahun);
                    if (ef == null) {
                        EnableFiture enableFiture = new EnableFiture();
                        enableFiture.setMahasiswa(mcicilan.getMahasiswa());
                        enableFiture.setTahunAkademik(tahun);
                        enableFiture.setFitur(StatusRecord.KRS);
                        enableFiture.setEnable(true);
                        enableFiture.setKeterangan("-");

                        enableFitureDao.save(enableFiture);
                    }
                    TahunAkademikProdi tahunProdi = tahunProdiDao.findByTahunAkademikAndProdi(tahun, mhs.getIdProdi());
                    Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(mhs, tahun, StatusRecord.AKTIF);
                    if (k == null) {
                        Krs krs = new Krs();
                        krs.setTahunAkademik(tahun);
                        krs.setTahunAkademikProdi(tahunProdi);
                        krs.setProdi(mcicilan.getMahasiswa().getIdProdi());
                        krs.setMahasiswa(mcicilan.getMahasiswa());
                        krs.setNim(mcicilan.getMahasiswa().getNim());
                        krs.setTanggalTransaksi(LocalDateTime.now());
                        krs.setStatus(StatusRecord.AKTIF);
                        krsDao.save(krs);
                    }
                }
            }
        }

    }

    public void requestCreateTagihan(Tagihan tagihan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(tagihan.getNilaiTagihan())
                .tahunAkademik(tagihan.getTahunAkademik().getId())
                .debitur(tagihan.getMahasiswa().getNim())
                .keterangan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " +tagihan.getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .jenisRequest(TagihanRequest.Type.CREATE)
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }

    public void editTagihan(Tagihan tagihan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(tagihan.getNilaiTagihan())
                .tahunAkademik(tagihan.getTahunAkademik().getId())
                .debitur(tagihan.getMahasiswa().getNim())
                .keterangan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " + tagihan.getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(LocalDate.now().plusYears(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .jenisRequest(TagihanRequest.Type.REPLACE)
                .nomorTagihanLama(tagihan.getNomor())
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }

    public void ubahJadiCicilan(RequestCicilan requestCicilan) {
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(requestCicilan.getTagihan().getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(requestCicilan.getNilaiCicilan())
                .tahunAkademik(requestCicilan.getTagihan().getTahunAkademik().getId())
                .debitur(requestCicilan.getTagihan().getMahasiswa().getNim())
                .keterangan(requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama()
                        + " a.n. " +requestCicilan.getTagihan().getMahasiswa().getNama())
                .tanggalJatuhTempo(Date.from(requestCicilan.getTanggalJatuhTempo().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .jenisRequest(TagihanRequest.Type.REPLACE)
                .nomorTagihanLama(requestCicilan.getTagihan().getNomor())
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }

    public void mengirimCicilanSelanjutnya(RequestCicilan requestCicilan) {
        String keterangan;
        if (requestCicilan.getTagihan().getNilaiTagihan().intValue() > 50000000){
            keterangan = "Cicilan biaya kuliah bulan " + requestCicilan.getTanggalJatuhTempo().getMonth() + " tahun " + requestCicilan.getTanggalJatuhTempo().getYear();
        }else{
            keterangan = requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getNama()
                    + " a.n. " +requestCicilan.getTagihan().getMahasiswa().getNama();
        }
        TagihanRequest tagihanRequest = TagihanRequest.builder()
                .kodeBiaya(requestCicilan.getTagihan().getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .jenisTagihan(requestCicilan.getTagihan().getNilaiJenisTagihan().getJenisTagihan().getId())
                .nilaiTagihan(requestCicilan.getNilaiCicilan())
                .tahunAkademik(requestCicilan.getTagihan().getTahunAkademik().getId())
                .debitur(requestCicilan.getTagihan().getMahasiswa().getNim())
                .keterangan(keterangan)
                .tanggalJatuhTempo(Date.from(requestCicilan.getTanggalJatuhTempo().atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .jenisRequest(TagihanRequest.Type.CREATE)
                .build();
        kafkaSender.requestCreateTagihan(tagihanRequest);
    }


    public void hapusTagihan(Tagihan tagihan){
        HapusTagihanRequest hapusTagihan = HapusTagihanRequest.builder()
                .nomorTagihan(tagihan.getNomor())
                .jenisTagihan(tagihan.getNilaiJenisTagihan().getJenisTagihan().getId())
                .debitur(tagihan.getMahasiswa().getNim())
                .kodeBiaya(tagihan.getNilaiJenisTagihan().getProdi().getKodeBiaya())
                .build();
        kafkaSender.requsetHapusTagihan(hapusTagihan);
    }

    public void prosesPembayaran(Tagihan tagihan, PembayaranTagihan pt){

        log.debug("Pembayaran Tagihan = {}", pt.toString());

        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setTagihan(tagihan);
        pembayaran.setNomorRekening(pt.getNomorRekening());
        pembayaran.setAmount(pt.getNilaiPembayaran());
        pembayaran.setWaktuBayar(LocalDateTime.parse(pt.getWaktuPembayaran(), FORMATTER_ISO_DATE_TIME));
        pembayaran.setReferensi(pt.getReferensiPembayaran());

        Bank bank = new Bank();
        bank.setId(pt.getBank());
        pembayaran.setBank(bank);

        BigDecimal akumulasi = tagihan.getAkumulasiPembayaran().add(pt.getNilaiPembayaran());
        BigDecimal nilai = tagihan.getNilaiTagihan();
        tagihan.setAkumulasiPembayaran(akumulasi);
        log.info("akumulasi : {}", akumulasi);
        if (akumulasi.compareTo(nilai) == 0){
            tagihan.setLunas(true);
            tagihan.setStatusTagihan(StatusTagihan.LUNAS);
            tagihanDao.save(tagihan);
            log.info("nomor tagihan {} LUNAS", tagihan.getNomor());
        }

        if (TAGIHAN_UTS.equals(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.UTS, false, tagihan.getTahunAkademik());
            if (enableFiture != null){
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        if (TAGIHAN_UAS.equals(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.UAS, false, tagihan.getTahunAkademik());
            if (enableFiture != null){
                enableFiture.setEnable(true);
                enableFitureDao.save(enableFiture);
            }
        }

        if (TAGIHAN_KRS.contains(pt.getKodeJenisBiaya())){
            EnableFiture enableFiture = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.KRS, false, tagihan.getTahunAkademik());
            if (enableFiture == null) {
                enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.KRS);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setKeterangan("-");
            }
            enableFiture.setEnable(true);
            enableFitureDao.save(enableFiture);

            TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(tagihan.getTahunAkademik(),
                    tagihan.getMahasiswa().getIdProdi());

            Krs krs = krsDao.findByMahasiswaAndTahunAkademikAndStatus(
                    tagihan.getMahasiswa(), tagihan.getTahunAkademik(), StatusRecord.AKTIF);

            if(krs == null) {
                createKrs(tagihan, tahunAkademikProdi);
            }
        }

        if (TAGIHAN_SEMPRO.equals(pt.getKodeJenisBiaya())){
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.SEMPRO, true, tagihan.getTahunAkademik());
            if (ef == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.SEMPRO);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setEnable(true);
                enableFiture.setKeterangan("-");
                enableFitureDao.save(enableFiture);
            } else if (ef.getEnable() == false) {
                ef.setEnable(true);
                ef.setKeterangan("Ngulang");
                enableFitureDao.save(ef);
            }
        }

        if (TAGIHAN_SKRIPSI.equals(pt.getKodeJenisBiaya())){
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnableAndTahunAkademik(tagihan.getMahasiswa(),
                    StatusRecord.SKRIPSI, true, tagihan.getTahunAkademik());
            if (ef == null) {
                EnableFiture enableFiture = new EnableFiture();
                enableFiture.setFitur(StatusRecord.SKRIPSI);
                enableFiture.setMahasiswa(tagihan.getMahasiswa());
                enableFiture.setTahunAkademik(tagihan.getTahunAkademik());
                enableFiture.setEnable(true);
                enableFiture.setKeterangan("-");
                enableFitureDao.save(enableFiture);
            } else if (ef.getEnable() == false) {
                ef.setEnable(true);
                ef.setKeterangan("Ngulang");
                enableFitureDao.save(ef);
            }
        }

        if (TAGIHAN_SP.equals(pt.getKodeJenisBiaya())) {
            List<PraKrsSp> listSp = praKrsSpDao.findByMahasiswaAndStatusAndStatusApproveAndTahunAkademik(tagihan.getMahasiswa(), StatusRecord.AKTIF, StatusApprove.APPROVED, tagihan.getTahunAkademik());
            for (PraKrsSp listApprove : listSp){
                Krs k = krsDao.findByMahasiswaAndTahunAkademikAndStatus(listApprove.getMahasiswa(), listApprove.getTahunAkademik(), StatusRecord.AKTIF);
                TahunAkademikProdi tahunAkademikProdi = tahunProdiDao.findByTahunAkademikAndProdi(listApprove.getTahunAkademik(), listApprove.getMahasiswa().getIdProdi());
                    if (k == null) {

                        createKrs(tagihan, tahunAkademikProdi);

                    }
            }
        }

        if (TAGIHAN_TAHFIDZ.equals(pt.getKodeJenisBiaya())) {
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnable(tagihan.getMahasiswa(), StatusRecord.TAHFIDZ, false);
            if (ef != null) {
                ef.setEnable(true);
                ef.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(ef);
            }
        }

        if (TAGIHAN_KOMPRE.equals(pt.getKodeJenisBiaya())) {
            EnableFiture ef = enableFitureDao.findByMahasiswaAndFiturAndEnable(tagihan.getMahasiswa(), StatusRecord.KOMPRE, false);
            if (ef != null) {
                ef.setEnable(true);
                ef.setTahunAkademik(tahunAkademikDao.findByStatus(StatusRecord.AKTIF));
                enableFitureDao.save(ef);
            }
        }

        tagihanDao.save(tagihan);
        pembayaranDao.save(pembayaran);
        log.debug("Pembayaran untuk tagihan {} berhasil disimpan", pt.getNomorTagihan());


        if (tagihan.getStatusTagihan() == StatusTagihan.DICICIL){

            RequestCicilan cicilanLunas = requestCicilanDao.findByTagihanAndStatusCicilanAndStatus(tagihan, StatusCicilan.SEDANG_DITAGIHKAN, StatusRecord.AKTIF);
            if (cicilanLunas != null) {
                cicilanLunas.setStatusCicilan(StatusCicilan.LUNAS);
                requestCicilanDao.save(cicilanLunas);
                log.info("cicilan lunas : {}", cicilanLunas.getTagihan().getKeterangan());

                User usr = cicilanLunas.getTagihan().getMahasiswa().getUser();
                if (usr.getRole().getId().equals("mahasiswanunggak")){
                    Role role = roleDao.findById("mahasiswa").get();
                    usr.setRole(role);
                    userDao.save(usr);
                }

            }

            RequestCicilan requestCicilan = requestCicilanDao.cariCicilanSelanjutnya(tagihan);
            if (requestCicilan == null){
                log.info("tidak ada cicilan");
            }else {
                requestCicilan.setStatusCicilan(StatusCicilan.SEDANG_DITAGIHKAN);
                requestCicilanDao.save(requestCicilan);
                mengirimCicilanSelanjutnya(requestCicilan);
                log.info("kirim cicilan selanjutnya : {}", requestCicilan.getNilaiCicilan());

                User usr = requestCicilan.getTagihan().getMahasiswa().getUser();
                if (usr.getRole().getId().equals("mahasiswanunggak")){
                    Role role = roleDao.findById("mahasiswa").get();
                    usr.setRole(role);
                    userDao.save(usr);
                }

            }
        }

    }

    public void createKrs(Tagihan tagihan, TahunAkademikProdi tahunAkademikProdi) {
        Krs krs = new Krs();
        krs.setTahunAkademik(tagihan.getTahunAkademik());
        krs.setTahunAkademikProdi(tahunAkademikProdi);
        krs.setProdi(tagihan.getMahasiswa().getIdProdi());
        krs.setMahasiswa(tagihan.getMahasiswa());
        krs.setNim(tagihan.getMahasiswa().getNim());
        krs.setTanggalTransaksi(LocalDateTime.now());
        krs.setStatus(StatusRecord.AKTIF);
        krsDao.save(krs);
    }

    private void createEnableFitur(@RequestParam(required = false) TahunAkademik tahun, Tagihan tgh, StatusRecord status, Boolean enabled) {
        EnableFiture enableFiture = new EnableFiture();
        enableFiture.setMahasiswa(tgh.getMahasiswa());
        enableFiture.setTahunAkademik(tahun);
        enableFiture.setFitur(status);
        enableFiture.setEnable(enabled);
        enableFiture.setKeterangan("-");

        enableFitureDao.save(enableFiture);
    }

    public void cretatTagihanMengulang(Mahasiswa mahasiswa, TahunAkademik tahunAkademik, String jenis){

        JenisTagihan jenisTagihan = null;
        BigDecimal nilai = null;

        if ("UjianTahfidz".equals(jenis)) {
            jenisTagihan = jenisTagihanDao.findByKodeAndStatus("16", StatusRecord.AKTIF);
            nilai = new BigDecimal(100000);
        }else if ("UjianKomprehensif".equals(jenis)){
            jenisTagihan = jenisTagihanDao.findByKodeAndStatus("17", StatusRecord.AKTIF);
            nilai = new BigDecimal(150000);
        }

        NilaiJenisTagihan cekJenisTagihan = nilaiJenisTagihanDao.findByProdiAndAngkatanAndTahunAkademikAndProgramAndStatusAndJenisTagihan(mahasiswa.getIdProdi(),
                mahasiswa.getAngkatan(), tahunAkademik, mahasiswa.getIdProgram(), StatusRecord.AKTIF, jenisTagihan);

        if (cekJenisTagihan == null){
            NilaiJenisTagihan jenisTagihanBaru = new NilaiJenisTagihan();
            jenisTagihanBaru.setJenisTagihan(jenisTagihan);
            jenisTagihanBaru.setNilai(nilai);
            jenisTagihanBaru.setTahunAkademik(tahunAkademik);
            jenisTagihanBaru.setProdi(mahasiswa.getIdProdi());
            jenisTagihanBaru.setProgram(mahasiswa.getIdProgram());
            jenisTagihanBaru.setAngkatan(mahasiswa.getAngkatan());
            nilaiJenisTagihanDao.save(jenisTagihanBaru);

            Tagihan tagihan = new Tagihan();
            tagihan.setMahasiswa(mahasiswa);
            tagihan.setNilaiJenisTagihan(jenisTagihanBaru);
            tagihan.setKeterangan("Tagihan " + jenisTagihanBaru.getJenisTagihan().getNama()
                    + " a.n. " + mahasiswa.getNama());
            tagihan.setNilaiTagihan(jenisTagihanBaru.getNilai());
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
            tagihan.setStatus(StatusRecord.AKTIF);
            tagihanDao.save(tagihan);
            requestCreateTagihan(tagihan);
        }else{
            Tagihan tagihan = new Tagihan();
            tagihan.setMahasiswa(mahasiswa);
            tagihan.setNilaiJenisTagihan(cekJenisTagihan);
            tagihan.setKeterangan("Tagihan " + cekJenisTagihan.getJenisTagihan().getNama()
                    + " a.n. " + mahasiswa.getNama());
            tagihan.setNilaiTagihan(cekJenisTagihan.getNilai());
            tagihan.setAkumulasiPembayaran(BigDecimal.ZERO);
            tagihan.setTanggalPembuatan(LocalDate.now());
            tagihan.setTanggalJatuhTempo(LocalDate.now().plusYears(1));
            tagihan.setTanggalPenangguhan(LocalDate.now().plusYears(1));
            tagihan.setTahunAkademik(tahunAkademik);
            tagihan.setStatusTagihan(StatusTagihan.AKTIF);
            tagihan.setStatus(StatusRecord.AKTIF);
            tagihanDao.save(tagihan);
            requestCreateTagihan(tagihan);
        }
    }

}
