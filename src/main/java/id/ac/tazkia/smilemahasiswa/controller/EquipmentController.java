package id.ac.tazkia.smilemahasiswa.controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import id.ac.tazkia.smilemahasiswa.dao.*;
import id.ac.tazkia.smilemahasiswa.dto.room.KelasMahasiswaDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import javax.persistence.criteria.CriteriaBuilder;
import javax.validation.Valid;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.List;

@Controller
public class EquipmentController {

    @Autowired
    private LembagaDao lembagaDao;

    @Autowired
    private GedungDao gedungDao;

    @Autowired
    private KampusDao kampusDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private KelasMahasiswaDao kelasMahasiswaDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private RuangJenisDao ruangJenisDao;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private BahasaDao bahasaDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Value("${upload.barcode}")
    private String uploadFolder;

    //    Attribute
    @ModelAttribute("angkatan")
    public Iterable<Mahasiswa> angkatan() {
        return mahasiswaDao.cariAngkatan();
    }

    @ModelAttribute("prodi")
    public Iterable<Prodi> prodi() {
        return prodiDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS));
    }

//    Institution

    @GetMapping("/equipment/institution/list")
    public void curriculumList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", lembagaDao.findByStatusNotInAndNamaLembagaContainingIgnoreCaseOrderByNamaLembaga(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("list", lembagaDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/equipment/institution/form")
    public void curriculumForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("lembaga", new Lembaga());

        if (id != null && !id.isEmpty()) {
            Lembaga lembaga = lembagaDao.findById(id).get();
            if (lembaga != null) {
                model.addAttribute("lembaga", lembaga);
                if (lembaga.getStatus() == null) {
                    lembaga.setStatus(StatusRecord.AKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/institution/form")
    public String prosesInstitution(@Valid Lembaga lembaga) {
        if (lembaga.getStatus() == null) {
            lembaga.setStatus(StatusRecord.AKTIF);
        }
        lembagaDao.save(lembaga);

        return "redirect:list";

    }

    @PostMapping("/equipment/institution/delete")
    public String deleteInnstitution(@RequestParam Lembaga lembaga) {
        lembaga.setStatus(StatusRecord.HAPUS);
        lembagaDao.save(lembaga);

        return "redirect:list";
    }

//    Building

    @GetMapping("/equipment/building/list")
    public void daftarGedung(Model model, @RequestParam(required = false) String search, Pageable page) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("list", gedungDao.findByStatusNotInAndAndNamaGedungContainingIgnoreCaseOrderByNamaGedung(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("list", gedungDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/equipment/building/form")
    public void gedungForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("gedung", new Gedung());
        model.addAttribute("kampus", kampusDao.findByStatus(StatusRecord.AKTIF));

        if (id != null && !id.isEmpty()) {
            Gedung gedung = gedungDao.findById(id).get();
            if (gedung != null) {
                model.addAttribute("gedung", gedung);
                if (gedung.getStatus() == null) {
                    gedung.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }


    @PostMapping("/equipment/building/form")
    public String prosesForm(@Valid Gedung gedung) {
        if (gedung.getStatus() == null) {
            gedung.setStatus(StatusRecord.NONAKTIF);
        }
        gedungDao.save(gedung);
        return "redirect:list";
    }

    @PostMapping("/equipment/building/delete")
    public String deleteLembaga(@RequestParam Gedung gedung) {
        gedung.setStatus(StatusRecord.HAPUS);
        gedungDao.save(gedung);

        return "redirect:list";
    }

//    Campus

    @GetMapping("/equipment/campus/list")
    public void kampusList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("seacrh", search);
            model.addAttribute("list", kampusDao.findByStatusNotInAndNamaKampusContainingIgnoreCaseOrderByNamaKampus(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("list", kampusDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }
    }

    @GetMapping("/equipment/campus/form")
    public void formKampus(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("kampus", new Kampus());

        if (id != null && !id.isEmpty()) {
            Kampus kampus = kampusDao.findById(id).get();
            if (kampus != null) {
                model.addAttribute("kampus", kampus);
                if (kampus.getStatus() == null) {
                    kampus.setStatus(StatusRecord.AKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/campus/form")
    public String prosesForm(@Valid Kampus kampus) {
        if (kampus.getStatus() == null) {
            kampus.setStatus(StatusRecord.NONAKTIF);
        }
        kampusDao.save(kampus);
        return "redirect:list";
    }

    @PostMapping("/equipment/campus/delete")
    public String deleteKampus(@RequestParam Kampus kampus) {
        kampus.setStatus(StatusRecord.HAPUS);
        kampusDao.save(kampus);

        return "redirect:list";
    }

    //    Room
    @GetMapping("/equipment/room/list")
    public void roomList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listRuang", ruanganDao.findByStatusNotInAndAndNamaRuanganContainingIgnoreCaseOrderByNamaRuangan(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("listRuang", ruanganDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));

        }
    }

    @GetMapping("/equipment/room/form")
    public void roomForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("ruangan", new Ruangan());
        model.addAttribute("ruangJenis", ruangJenisDao.findByStatus(StatusRecord.AKTIF));
        model.addAttribute("gedung", gedungDao.findByStatus(StatusRecord.AKTIF));


        if (id != null && !id.isEmpty()) {
            Ruangan ruangan = ruanganDao.findById(id).get();
            if (ruangan != null) {
                model.addAttribute("ruangan", ruangan);
                if (ruangan.getStatus() == null) {
                    ruangan.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/room/form")
    public String prosesForm(@Valid Ruangan ruangan) {
        if (ruangan.getStatus() == null) {
            ruangan.setStatus(StatusRecord.NONAKTIF);
        }
        ruanganDao.save(ruangan);
        return "redirect:list";
    }

    @PostMapping("/equipment/room/delete")
    public String deleteRuangan(@RequestParam Ruangan ruangan) {
        ruangan.setStatus(StatusRecord.HAPUS);
        ruanganDao.save(ruangan);

        return "redirect:list";
    }


    //    class
    @GetMapping("/equipment/class/list")
    public void classList(Model model, @PageableDefault(size = 10) Pageable page, String search) {
        if (StringUtils.hasText(search)) {
            model.addAttribute("search", search);
            model.addAttribute("listKelas", kelasDao.findByStatusNotInAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(Arrays.asList(StatusRecord.HAPUS), search, page));
        } else {
            model.addAttribute("listKelas", kelasDao.findByStatusNotIn(Arrays.asList(StatusRecord.HAPUS), page));
        }

    }

    @GetMapping("/equipment/class/form")
    public void classForm(Model model, @RequestParam(required = false) String id) {
        model.addAttribute("kelas", new Kelas());


        if (id != null && !id.isEmpty()) {
            Kelas kelas = kelasDao.findById(id).get();
            if (kelas != null) {
                model.addAttribute("bahasa", bahasaDao.findByStatusOrderByBahasa(StatusRecord.AKTIF));
                model.addAttribute("kelas", kelas);
                if (kelas.getStatus() == null) {
                    kelas.setStatus(StatusRecord.NONAKTIF);
                }
            }
        }
    }

    @PostMapping("/equipment/class/form")
    public String prosesClass(@ModelAttribute @Valid Kelas kelas) {
        if (kelas.getStatus() == null) {
            kelas.setStatus(StatusRecord.NONAKTIF);
        }
        kelasDao.save(kelas);
        return "redirect:list";
    }

    @PostMapping("/equipment/class/delete")
    public String deleteKelas(@RequestParam Kelas kelas,
                              RedirectAttributes attributes) {

        Integer jmlJdwal = jadwalDao.jmlJadwal(kelas.getId());

        if (jmlJdwal == null) {
            jmlJdwal = 0;
        }

        if (jmlJdwal > 0) {
            attributes.addFlashAttribute("gagal", "Save Data Berhasil");
            return "redirect:list";
        } else {
            kelas.setStatus(StatusRecord.HAPUS);
            kelasDao.save(kelas);
        }
        attributes.addFlashAttribute("success", "Save Data Berhasil");
        return "redirect:list";

    }

    @GetMapping("/equipment/class/view")
    public void viewMahasiswa(@RequestParam Kelas kelas, Model model, Pageable page) {
        Iterable<KelasMahasiswa> kelasMahasiswas = kelasMahasiswaDao.findByKelasAndStatus(kelas, StatusRecord.AKTIF);
        model.addAttribute("kelasMahasiswa", kelasMahasiswas);
    }

    @GetMapping("/equipment/class/mahasiswa")
    public void ruanganMahasiswa(@RequestParam(required = false) String angkatan, @RequestParam Kelas kelas, @RequestParam(required = false) Prodi prodi, @RequestParam(required = false) Konsentrasi konsentrasi, Model model, Pageable page) {
        model.addAttribute("selectedKelas", kelas);
        String ak = angkatan;
        String pro = prodi.getId();
        if (prodi != null) {
            List<KelasMahasiswaDto> mahasiswaDtos = new ArrayList<>();
            model.addAttribute("selected", prodi);
            model.addAttribute("selectedAngkatan", angkatan);
            model.addAttribute("kelas", kelas.getId());
            model.addAttribute("namaKelas", kelas.getNamaKelas());
            model.addAttribute("kelasAngkatan", kelasDao.kelasAngktanProdi(pro, ak));
            model.addAttribute("konsentrasi", konsentrasiDao.konsentrasiProdi(prodi.getId()));
            if (konsentrasi != null) {
                Iterable<Mahasiswa> mahasiswaKonsentrasi = mahasiswaDao.carikelasKonsentrai(StatusRecord.AKTIF, angkatan, prodi, konsentrasi);
                if (mahasiswaKonsentrasi != null) {
                    for (Mahasiswa m : mahasiswaKonsentrasi) {
                        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(m, StatusRecord.AKTIF);
                        if (kelasMahasiswa == null) {
                            KelasMahasiswaDto km = new KelasMahasiswaDto();
                            km.setId(m.getId());
                            km.setNama(m.getNama());
                            if (m.getKurikulum() != null) {
                                km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                            } else {
                                km.setKurikulum("");
                            }
                            km.setNim(m.getNim());
                            km.setKelas("");
                            if (m.getIdKonsentrasi() == null) {
                                km.setKonsentrasi(null);
                            } else {
                                if (m.getIdKonsentrasi().getStatus() == StatusRecord.AKTIF) {
                                    km.setKonsentrasi(m.getIdKonsentrasi().getNamaKonsentrasi());
                                }
                            }

                            mahasiswaDtos.add(km);
                        }
                        if (kelasMahasiswa != null) {
                            KelasMahasiswaDto km = new KelasMahasiswaDto();
                            km.setId(m.getId());
                            km.setNama(m.getNama());
                            if (m.getKurikulum() != null) {
                                km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                            } else {
                                km.setKurikulum("");
                            }
                            km.setNim(m.getNim());
                            km.setKelas(kelasMahasiswa.getKelas().getNamaKelas());
                            if (m.getIdKonsentrasi() == null) {
                                km.setKonsentrasi(null);
                            } else {
                                if (m.getIdKonsentrasi().getStatus() == StatusRecord.AKTIF) {
                                    km.setKonsentrasi(m.getIdKonsentrasi().getNamaKonsentrasi());
                                }
                            }

                            mahasiswaDtos.add(km);

                        }
                    }
                }

            } else {
                Iterable<Mahasiswa> mahasiswaAll = mahasiswaDao.carikelas(StatusRecord.AKTIF, angkatan, prodi);

                if (mahasiswaAll != null) {
                    for (Mahasiswa m : mahasiswaAll) {
                        KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndStatus(m, StatusRecord.AKTIF);
                        if (kelasMahasiswa == null) {
                            KelasMahasiswaDto km = new KelasMahasiswaDto();
                            km.setId(m.getId());
                            km.setNama(m.getNama());
                            if (m.getKurikulum() != null) {
                                km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                            } else {
                                km.setKurikulum("");
                            }
                            km.setNim(m.getNim());
                            km.setKelas("");
                            if (m.getIdKonsentrasi() == null) {
                                km.setKonsentrasi(null);
                            } else if (m.getIdKonsentrasi().getStatus() == StatusRecord.AKTIF) {
                                km.setKonsentrasi(m.getIdKonsentrasi().getNamaKonsentrasi());
                            }

                            mahasiswaDtos.add(km);
                        }
                        if (kelasMahasiswa != null) {
                            KelasMahasiswaDto km = new KelasMahasiswaDto();
                            km.setId(m.getId());
                            km.setNama(m.getNama());
                            if (m.getKurikulum() != null) {
                                km.setKurikulum(m.getKurikulum().getNamaKurikulum());
                            } else {
                                km.setKurikulum("");
                            }
                            km.setNim(m.getNim());
                            km.setKelas(kelasMahasiswa.getKelas().getNamaKelas());
                            if (m.getIdKonsentrasi() == null) {
                                km.setKonsentrasi(null);
                            } else {
                                if (m.getIdKonsentrasi().getStatus() == StatusRecord.AKTIF) {
                                    km.setKonsentrasi(m.getIdKonsentrasi().getNamaKonsentrasi());
                                }
                            }

                            mahasiswaDtos.add(km);

                        }
                    }
                }
            }

            model.addAttribute("mahasiswaList", mahasiswaDtos);
        }
    }

    @PostMapping("/equipment/class/mahasiswa")
    public String saveruanganMahasiswa(@RequestParam String[] data, @RequestParam Kelas kelas) {

        for (String idMahasiswa : data) {
            Mahasiswa mahasiswa = mahasiswaDao.findById(idMahasiswa).get();
            KelasMahasiswa kelasMahasiswa = kelasMahasiswaDao.findByMahasiswaAndKelas(mahasiswa, kelas);
            if (kelasMahasiswa != null) {
                KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
                if (kelasMhsw != null) {
                    kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                    kelasMahasiswaDao.save(kelasMhsw);
                }
                kelasMahasiswa.setStatus(StatusRecord.AKTIF);
                kelasMahasiswaDao.save(kelasMahasiswa);

            } else {
                KelasMahasiswa kelasMhsw = kelasMahasiswaDao.findByMahasiswaAndStatus(mahasiswa, StatusRecord.AKTIF);
                if (kelasMhsw != null) {
                    kelasMhsw.setStatus(StatusRecord.NONAKTIF);
                    kelasMahasiswaDao.save(kelasMhsw);
                }
                KelasMahasiswa km = new KelasMahasiswa();
                km.setStatus(StatusRecord.AKTIF);
                km.setKelas(kelas);
                km.setMahasiswa(mahasiswa);
                kelasMahasiswaDao.save(km);
            }


        }
        return "redirect:list";

    }

    @GetMapping("/refrshBarcode")
    public String testBarcode(@RequestParam(name = "id", value = "id") PresensiDosen presensiDosen) throws Exception {
        Integer qrCodeSize = 300;
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("ss");
        String productCode = dateFormat.format(date);


        Date date1 = Calendar.getInstance().getTime();
        DateFormat dateFormat1 = new SimpleDateFormat("mm");
        String productionYear = dateFormat1.format(date1);

        Date date2 = Calendar.getInstance().getTime();
        DateFormat dateFormat2 = new SimpleDateFormat("MM");
        String productCode2 = dateFormat2.format(date2);

        Integer productNumberLength = 6;
        Integer productQuantity = 1;

        String folderOutput = uploadFolder;
        new File(folderOutput).mkdirs();

        for (int i = 1; i <= productQuantity; i++) {
            String presensiBarcode = productCode + productionYear + productCode2
                    + String.format("%1$" + productNumberLength + "s", i)
                    .replace(' ', '2');
            System.out.println("Generate QR Code for product number " + presensiBarcode);
            BufferedImage qrCode = generateQRcode("http://localhost:8080/presensi/" + presensiDosen.getId() + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
//            BufferedImage qrCode = generateQRcode("https://smile.tazkia.ac.id/presensi/"+ presensiDosen.getId()  + "?barcode=" + presensiBarcode + ".png", qrCodeSize);
            ImageIO.write(qrCode, "png",
                    new File(folderOutput + File.separator
                            + presensiBarcode + ".png"));
            if (presensiDosen.getBarcodeNow() == null) {
                presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                presensiDosenDao.save(presensiDosen);
            } else {
                File file = new File(uploadFolder + File.separator + presensiDosen.getBarcodeBefore());
                file.delete();
                presensiDosen.setBarcodeBefore(presensiDosen.getBarcodeNow());
                presensiDosen.setBarcodeNow(presensiBarcode + ".png");
                presensiDosenDao.save(presensiDosen);

            }
//            aset.setBarcode(productCode + productCode2 + "/" +productNumber + ".png");
        }
        return "redirect:admin";
    }

    private static BufferedImage generateQRcode(String barcodeText, Integer size) throws Exception {
        Map<EncodeHintType, ErrorCorrectionLevel> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.QR_CODE,
                size, size, hints);
        BufferedImage qrCode = MatrixToImageWriter.toBufferedImage(bitMatrix);

        int startingYposition = size - 2;

        Graphics2D g = (Graphics2D) qrCode.getGraphics();
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 5));
        Color color = Color.BLACK;
        g.setColor(color);

        FontMetrics fm = g.getFontMetrics();
        g.drawString(barcodeText, (qrCode.getWidth() / 2) - (fm.stringWidth(barcodeText) / 2), startingYposition);
        return qrCode;
    }

    @GetMapping("/studiesActivity/attendance/barcode")
    public void viewBarcode(@RequestParam(name = "id", value = "id") PresensiDosen presensiDosen, Model model) {
        model.addAttribute("presensi", presensiDosen);
    }

    @GetMapping("/sukses-absen/{id}")
    public String alertSukses(@PathVariable String id, Model model) {
        if (StringUtils.hasText(id)) {
            Optional<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findById(id);
            if (!presensiMahasiswa.isPresent()) {
                return "redirect:../dashboard";
            } else {
                model.addAttribute("detail", presensiMahasiswa.get());
                model.addAttribute("jamMasuk", presensiMahasiswa.get().getWaktuMasuk().getHour() + " : " + presensiMahasiswa.get().getWaktuMasuk().getMinute());
                return "study/success";
            }
        } else {
            return "redirect:../dashboard";
        }

    }

    @GetMapping("/terlambat-absen/{id}")
    public String alertTerlambat(@PathVariable String id, Model model) {
        if (StringUtils.hasText(id)) {
            Optional<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findById(id);
            if (!presensiMahasiswa.isPresent()) {
                return "redirect:../dashboard";
            } else {
                model.addAttribute("detail", presensiMahasiswa.get());
                model.addAttribute("jamMasuk", presensiMahasiswa.get().getWaktuMasuk().getHour() + " : " + presensiMahasiswa.get().getWaktuMasuk().getMinute());
                return "study/terlambat";
            }
        } else {
            return "redirect:../dashboard";
        }

    }

    @GetMapping("/study/expired")
    public void alertExpired() {

    }

    @GetMapping("/uploaded/{presensi}/barcode/")
    public ResponseEntity<byte[]> tampilkanEvidence(@PathVariable(name = "presensi", value = "presensi") PresensiDosen presensiDosen) throws Exception {
        String lokasiFile = uploadFolder + File.separator + presensiDosen.getBarcodeNow();

        try {
            HttpHeaders headers = new HttpHeaders();
            if (presensiDosen.getBarcodeNow().toLowerCase().endsWith("jpeg") || presensiDosen.getBarcodeNow().toLowerCase().endsWith("jpg")) {
                headers.setContentType(MediaType.IMAGE_JPEG);
            } else if (presensiDosen.getBarcodeNow().toLowerCase().endsWith("png")) {
                headers.setContentType(MediaType.IMAGE_PNG);
            } else if (presensiDosen.getBarcodeNow().toLowerCase().endsWith("pdf")) {
                headers.setContentType(MediaType.APPLICATION_PDF);
            } else {
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            byte[] data = Files.readAllBytes(Paths.get(lokasiFile));
            return new ResponseEntity<byte[]>(data, headers, HttpStatus.OK);
        } catch (Exception err) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();


        }

    }

    @GetMapping("/presensi/{id}")
    public String presensiMahasiswaBarcode(@PathVariable String id, Authentication authentication, @RequestParam String barcode) {

        User user = currentUserService.currentUser(authentication);
        Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
        PresensiDosen presensiDosen = presensiDosenDao.findById(id).get();
        SesiKuliah sesiKuliah = sesiKuliahDao.findByPresensiDosen(presensiDosen);

        if (barcode.equals(presensiDosen.getBarcodeNow()) || barcode.equals(presensiDosen.getBarcodeBefore())) {
            PresensiMahasiswa presensiMahasiswa = presensiMahasiswaDao.findByMahasiswaAndSesiKuliahAndStatus(mahasiswa, sesiKuliah, StatusRecord.AKTIF);
            if (LocalTime.now().plusHours(7).compareTo(presensiDosen.getJadwal().getJamMulai().plusMinutes(15)) >= 0){
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now().plusHours(7));
                presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),presensiMahasiswa.getKrsDetail().getJadwal().getJamSelesai()));
                presensiMahasiswa.setStatusPresensi(StatusPresensi.MANGKIR);
                presensiMahasiswaDao.save(presensiMahasiswa);
                return "redirect:../terlambat-absen/" + presensiMahasiswa.getId();

            }else {
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now().plusHours(7));
                presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),presensiMahasiswa.getKrsDetail().getJadwal().getJamSelesai()));
                presensiMahasiswa.setStatusPresensi(StatusPresensi.HADIR);
                presensiMahasiswaDao.save(presensiMahasiswa);
                return "redirect:../sukses-absen/" + presensiMahasiswa.getId();

            }


        } else {

            return "redirect:../study/expired";

        }

    }

}
