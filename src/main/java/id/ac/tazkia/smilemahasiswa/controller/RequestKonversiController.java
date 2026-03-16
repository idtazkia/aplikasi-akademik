package id.ac.tazkia.smilemahasiswa.controller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import groovy.util.logging.Slf4j;
import id.ac.tazkia.smilemahasiswa.constant.Approval;
import id.ac.tazkia.smilemahasiswa.dao.DetailKonfigurasiKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.DetailPendaftaranKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.FilePendaftaranKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.GradeDao;
import id.ac.tazkia.smilemahasiswa.dao.KonfigurasiKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.KrsDetailDao;
import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.PendaftaranRequestKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.RequestKonversiDao;
import id.ac.tazkia.smilemahasiswa.dao.RequestKonversiTenggatDao;
import id.ac.tazkia.smilemahasiswa.dao.SistemPenilaianKonversiDao;
import id.ac.tazkia.smilemahasiswa.dto.api.BaseResponseDto;
import id.ac.tazkia.smilemahasiswa.dto.konversi.DataRequestKonversi;
import id.ac.tazkia.smilemahasiswa.dto.konversi.GetRequestKonversi;
import id.ac.tazkia.smilemahasiswa.dto.requestkonversi.DetailKonfigurasiKonversiDto;
import id.ac.tazkia.smilemahasiswa.dto.transkript.TranskriptSementara;
import id.ac.tazkia.smilemahasiswa.entity.DetailKonfigurasiKonversi;
import id.ac.tazkia.smilemahasiswa.entity.DetailPendaftaranKonversi;
import id.ac.tazkia.smilemahasiswa.entity.FilePendaftaranKonversi;
import id.ac.tazkia.smilemahasiswa.entity.Grade;
import id.ac.tazkia.smilemahasiswa.entity.KonfigurasiKonversi;
import id.ac.tazkia.smilemahasiswa.entity.KrsDetail;
import id.ac.tazkia.smilemahasiswa.entity.Mahasiswa;
import id.ac.tazkia.smilemahasiswa.entity.PendaftaranRequestKonversi;
import id.ac.tazkia.smilemahasiswa.entity.Prodi;
import id.ac.tazkia.smilemahasiswa.entity.RequestKonversiTenggat;
import id.ac.tazkia.smilemahasiswa.entity.SistemPenilaianKonversi;
import id.ac.tazkia.smilemahasiswa.entity.StatusRecord;
import id.ac.tazkia.smilemahasiswa.entity.User;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import id.ac.tazkia.smilemahasiswa.service.FileService;

@Controller
@Slf4j
public class RequestKonversiController {

        @Value("${upload.request.konversi}")
        private String uploadFolder;
        @Autowired
        private ProdiDao prodiDao;
        @Autowired
        private RequestKonversiDao requestKonversiDao;
        @Autowired
        private KonfigurasiKonversiDao konfigurasiKonversiDao;
        @Autowired
        private DetailKonfigurasiKonversiDao detailKonfigurasiKonversiDao;
        @Autowired
        private SistemPenilaianKonversiDao sistemPenilaianKonversiDao;
        @Autowired
        private CurrentUserService currentUserService;
        @Autowired
        private MahasiswaDao mahasiswaDao;
        @Autowired
        private PendaftaranRequestKonversiDao pendaftaranRequestKonversiDao;
        @Autowired
        private FileService fileService;
        @Autowired
        private FilePendaftaranKonversiDao filePendaftaranKonversiDao;
        @Autowired
        private KrsDetailDao krsDetailDao;
        @Autowired
        private GradeDao gradeDao;
        @Autowired
        private DetailPendaftaranKonversiDao detailPendaftaranKonversiDao;
        @Autowired
        private RequestKonversiTenggatDao requestKonversiTenggatDao;

        @ModelAttribute("prodi")
        public Iterable<Prodi> prodi() {
                return prodiDao.findByStatus(StatusRecord.AKTIF);
        }

        @GetMapping("/request-konversi2/{jenis}")
        public String listRequestKonversi(@PathVariable String jenis, Model model,
                        @RequestParam(required = false) String prodi,
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) String sort,
                        @RequestParam(required = false) String order,
                        @RequestParam(required = false) Integer offset,
                        @RequestParam(required = false) Integer limit, Pageable pageable) {
                model.addAttribute("jenis", jenis);

                return "requestKonversi/list";
        }

        @GetMapping("/request-konversi/kemahasiswaan")
        public String listRequestKonversiKemahasiswaan(Model model) {

                return "requestKonversi/kemahasiswaan/list";
        }

        @GetMapping("/request-konversi/tqc")
        public String listRequestKonversiTqc(Model model) {

                return "requestKonversi/tqc/list";
        }

        @GetMapping("/request-konversi/prodi")
        public String listRequestKonversiProdi(Model model) {

                return "requestKonversi/prodi/list";
        }

        @GetMapping("/request-konversi/admin")
        public String listRequestKonversiAdmin(Model model) {
                model.addAttribute("tanggalKonversi",
                                requestKonversiTenggatDao.findByStatus(StatusRecord.AKTIF));

                return "requestKonversi/admin/list";
        }

        @GetMapping("/api/get-data-konversi/{jenis}")
        @ResponseBody
        public DataRequestKonversi getDataKonversi(@PathVariable String jenis,
                        @RequestParam(required = false) String prodiId,
                        @RequestParam(required = false, defaultValue = "") String search,
                        @RequestParam(required = false) String tanggalMulai,
                        @RequestParam(required = false) String tanggalSelesai,
                        @RequestParam(required = false) Integer offset,
                        @RequestParam(required = false) Integer limit) {

                LocalDateTime startDate = StringUtils.hasText(tanggalMulai)
                                ? LocalDate.parse(tanggalMulai).atStartOfDay()
                                : LocalDate.of(2000, 1, 1).atStartOfDay();
                LocalDateTime endDate = StringUtils.hasText(tanggalSelesai)
                                ? LocalDate.parse(tanggalSelesai).atTime(23, 59, 59)
                                : LocalDate.now().atTime(23, 59, 59);

                List<String> prodi = !StringUtils.hasText(prodiId) || prodiId.equals("-")
                                || prodiId == null
                                                ? prodiDao.findByStatus(StatusRecord.AKTIF).stream()
                                                                .map(Prodi::getId)
                                                                .collect(Collectors.toList())
                                                : List.of(prodiId);

                Map<String, List<String>> approvalMap = Map.of(
                                "tqc", List.of(Approval.WAITING.name()),
                                "kps", List.of(Approval.APPROVED_KEMAHASISWAAN.name(), Approval.APPROVED_TQC.name()),
                                "kemahasiswaan", List.of(Approval.WAITING.name()),
                                "admin", List.of(Approval.WAITING.name(),
                                                Approval.APPROVED_KEMAHASISWAAN.name(),
                                                Approval.APPROVED_TQC.name(),
                                                Approval.APPROVED.name()));

                List<GetRequestKonversi> data;
                switch (jenis.toLowerCase()) {
                        case "kemahasiswaan":
                                data = pendaftaranRequestKonversiDao.listRequestKonversiKemahasiswaan(search, prodi,
                                                approvalMap.get(jenis.toLowerCase()), startDate, endDate, limit,
                                                offset);
                                break;
                        case "tqc":
                                data = pendaftaranRequestKonversiDao.listRequestKonversiTqc(search, prodi,
                                                approvalMap.get(jenis.toLowerCase()), startDate, endDate, limit,
                                                offset);
                                break;
                        default:
                                if (approvalMap.containsKey(jenis.toLowerCase())) {
                                        data = pendaftaranRequestKonversiDao.listRequestKonversi(search, prodi,
                                                        approvalMap.get(jenis.toLowerCase()), startDate, endDate, limit,
                                                        offset);
                                } else {
                                        data = List.of();
                                }
                                break;
                }

                List<GetRequestKonversi> dataNotFilter = pendaftaranRequestKonversiDao.listRequestKonversiNotFilter(
                                search,
                                prodi, approvalMap.get(jenis.toLowerCase()), startDate, endDate);
                DataRequestKonversi response = new DataRequestKonversi();
                response.setTotal(dataNotFilter.size());
                response.setTotalNotFiltered(dataNotFilter.size());
                response.setRows(data);

                return response;
        }

        @GetMapping("/api/request-konversi-mahasiswa/{jenis}")
        @ResponseBody
        public List<KonfigurasiKonversi> getListKonfigurasiJenis(@PathVariable String jenis) {
                return konfigurasiKonversiDao.findByJenis(KonfigurasiKonversi.jenis.valueOf(jenis));
        }

        @GetMapping("/api/request-detail-konversi-mahasiswa/{id}")
        @ResponseBody
        public List<Map<String, Object>> getListDetailPendaftar(@PathVariable String id) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                List<Map<String, Object>> result = new ArrayList<>();
                for (DetailPendaftaranKonversi detail : pendaftaranRequestKonversi.getDetailPendaftaranKonversiList()) {
                        Map<String, Object> map = new LinkedHashMap<>();
                        map.put("id", detail.getId());
                        map.put("matakuliah", detail.getMatakuliah());
                        map.put("gradeLama", detail.getGradeLama());
                        map.put("gradeBaru", detail.getGradeBaru());
                        map.put("sks", detail.getKrsDetail() != null
                                        && detail.getKrsDetail().getMatakuliahKurikulum() != null
                                                        ? detail.getKrsDetail().getMatakuliahKurikulum().getJumlahSks()
                                                        : null);
                        map.put("status", detail.getStatus());
                        result.add(map);
                }
                return result;
        }

        @GetMapping("/api/request-total-sks-mahasiswa/{id}")
        @ResponseBody
        public Integer getTotalSksMahasiswa(@PathVariable String id) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();

                return pendaftaranRequestKonversiDao
                                .totalSksKonversi(pendaftaranRequestKonversi.getMahasiswa());
        }

        @GetMapping("/api/detail-request-konversi/{bentuk}")
        @ResponseBody
        public List<DetailKonfigurasiKonversi> getListDetailKonversi(@PathVariable String bentuk) {

                return detailKonfigurasiKonversiDao
                                .findByKonfigurasiKonversiOrderByNumberAscLevelNumberAsc(
                                                konfigurasiKonversiDao.findById(bentuk).get());
        }

        @GetMapping("/api/get-level-konversi/{bentuk}")
        @ResponseBody
        public List<String> getListLevelKonversi(@PathVariable String bentuk) {

                return detailKonfigurasiKonversiDao
                                .getLevel(konfigurasiKonversiDao.findById(bentuk).get());
        }

        @GetMapping("/api/detail-konversi/{id}")
        @ResponseBody
        public DetailKonfigurasiKonversi getDetailKonversi(@PathVariable String id) {

                return detailKonfigurasiKonversiDao.findById(id).get();
        }

        @GetMapping("/api/detail-konfigurasi-konversi/{idKonfigurasiKonversi}")
        @ResponseBody
        public List<DetailKonfigurasiKonversiDto> getDetailKonversi(
                        @PathVariable("idKonfigurasiKonversi") KonfigurasiKonversi konfigurasiKonversi) {
                return detailKonfigurasiKonversiDao.getDetailKonversi(konfigurasiKonversi);
        }

        @GetMapping("/api/sistem-penilaian/{idKonfigurasiKonversi}")
        @ResponseBody
        public List<SistemPenilaianKonversi> getSistemPenilaianKonversi(
                        @PathVariable("idKonfigurasiKonversi") KonfigurasiKonversi konfigurasiKonversi) {
                return sistemPenilaianKonversiDao
                                .findByKonfigurasiKonversiOrderByOrderAsc(konfigurasiKonversi);
        }

        @GetMapping("/api/range-nilai/{idKonfigurasiKonversi}")
        @ResponseBody
        public SistemPenilaianKonversi getRangeNilai(
                        @PathVariable("idKonfigurasiKonversi") KonfigurasiKonversi konfigurasiKonversi,
                        @RequestParam Integer point) {
                return sistemPenilaianKonversiDao.findByPointBawahAndPointAtas(konfigurasiKonversi,
                                point);
        }

        @GetMapping("/api/konfigurasi-konversi/{id}")
        @ResponseBody
        public KonfigurasiKonversi getRangeNilai(@PathVariable("id") String id) {
                return konfigurasiKonversiDao.findById(id).get();
        }

        @GetMapping("/api/get-matakuiah-konversi/{sks}")
        @ResponseBody
        public List<TranskriptSementara> getMatakuliahKonversi(@PathVariable("sks") Integer sks,
                        Authentication authentication) {
                User user = currentUserService.currentUser(authentication);
                Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                return krsDetailDao.matakuliahKonversi(mahasiswa, sks);
        }

        @GetMapping("/request-konversi/mahasiswa/list")
        public String listRequestKonversiMahasiswa(Model model) {

                return "requestKonversi/mahasiswa/list";
        }

        @GetMapping("/request-konversi/mahasiswa/matakuliah/{id}")
        public String listMatakuliahKonversi(Model model, @PathVariable("id") String id) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                model.addAttribute("pendaftaranRequestKonversi", pendaftaranRequestKonversi);

                return "requestKonversi/mahasiswa/matakuliah";
        }

        @PostMapping("/request-konversi/proses")
        public String prosesRequestKonversi(@RequestParam("totalSks") Integer sks,
                        @RequestParam("bentuk") String bentuk, Authentication authentication,
                        @RequestParam Map<String, String> params,
                        @RequestParam Map<String, MultipartFile> files) throws Exception {
                User user = currentUserService.currentUser(authentication);
                Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);

                PendaftaranRequestKonversi pendaftaranRequestKonversi = new PendaftaranRequestKonversi();
                pendaftaranRequestKonversi.setMahasiswa(mahasiswa);
                pendaftaranRequestKonversi.setKonfigurasiKonversi(
                                konfigurasiKonversiDao.findById(bentuk).get());
                pendaftaranRequestKonversi.setTotalSks(sks);
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);

                String lokasiUpload = uploadFolder + File.separator + mahasiswa.getNim()
                                + File.separator
                                + pendaftaranRequestKonversi.getKonfigurasiKonversi().getNama();
                for (int i = 0; params.containsKey("kategori-" + i); i++) {
                        String kategori = params.get("kategori-" + i);
                        MultipartFile file = files.get("file-" + i);
                        String fileName = fileService.uploadFile(file, lokasiUpload);
                        DetailKonfigurasiKonversi detailKonfigurasiKonversi = detailKonfigurasiKonversiDao
                                        .findById(kategori).get();
                        FilePendaftaranKonversi filePendaftaranKonversi = new FilePendaftaranKonversi();
                        filePendaftaranKonversi.setPendaftaranRequestKonversi(pendaftaranRequestKonversi);
                        filePendaftaranKonversi.setDetailKonfigurasiKonversi(detailKonfigurasiKonversi);
                        filePendaftaranKonversi.setFile(fileName);
                        filePendaftaranKonversiDao.save(filePendaftaranKonversi);

                }

                return "redirect:mahasiswa/matakuliah/" + pendaftaranRequestKonversi.getId();
        }

        @PostMapping("/request-konversi/proses-matakuliah")
        public String prosesRequestKonversiMatakuliah(@RequestParam String sistem,
                        @RequestParam String id, @RequestParam Map<String, String> params) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                pendaftaranRequestKonversi.setStatus(Approval.WAITING);

                if (sistem.equals("POINT")) {
                        SistemPenilaianKonversi sistemPenilaianKonversi = sistemPenilaianKonversiDao
                                        .cekPenilaianKonversi(pendaftaranRequestKonversi.getKonfigurasiKonversi(),
                                                        pendaftaranRequestKonversi.getTotalSks());
                        for (int i = 0; params.containsKey("krsdetail-" + i); i++) {
                                String result = params.get("result-" + i);
                                if (result != null) {
                                        String krsDetailId = params.get("krsdetail-" + i);
                                        KrsDetail krsDetail = krsDetailDao.findById(krsDetailId).get();
                                        FilePendaftaranKonversi filePendaftaranKonversi = filePendaftaranKonversiDao
                                                        .findByPendaftaranRequestKonversi(pendaftaranRequestKonversi);

                                        DetailPendaftaranKonversi detailPendaftaranKonversi = new DetailPendaftaranKonversi();
                                        detailPendaftaranKonversi
                                                        .setPendaftaranRequestKonversi(pendaftaranRequestKonversi);
                                        detailPendaftaranKonversi.setKrsDetail(krsDetail);
                                        detailPendaftaranKonversi.setGradeLama(krsDetail.getGrade());
                                        detailPendaftaranKonversi
                                                        .setMatakuliah(krsDetail.getMatakuliahKurikulum()
                                                                        .getMatakuliah().getNamaMatakuliah());
                                        detailPendaftaranKonversi.setKeterangan("Pendaftran Konversi Jalur "
                                                        + filePendaftaranKonversi.getDetailKonfigurasiKonversi()
                                                                        .getNama());
                                        if (sistemPenilaianKonversi.getOpsiNilai().equals(
                                                        SistemPenilaianKonversi.OpsiNilai.PLUS)) {
                                                Grade grade = gradeDao.findByNama(krsDetail.getGrade());
                                                int valueNilai = grade.getNilai() + sistemPenilaianKonversi.getNilai();
                                                Grade maxGrade = gradeDao.findByNilai(gradeDao.maxNilai());
                                                String newGrade = valueNilai > maxGrade.getNilai() ? maxGrade.getNama()
                                                                : gradeDao.findByNilai(valueNilai).getNama();
                                                detailPendaftaranKonversi.setGradeBaru(newGrade);
                                        }
                                        detailPendaftaranKonversiDao
                                                        .save(detailPendaftaranKonversi);
                                }
                        }
                }

                if (sistem.equals("LEVEL") || sistem.equals("ASSESMENT")) {
                        for (int i = 0; params.containsKey("krsdetail-" + i); i++) {
                                String result = params.get("result-" + i);
                                if (result != null) {
                                        String krsDetailId = params.get("krsdetail-" + i);
                                        KrsDetail krsDetail = krsDetailDao.findById(krsDetailId).get();
                                        FilePendaftaranKonversi filePendaftaranKonversi = filePendaftaranKonversiDao
                                                        .findByPendaftaranRequestKonversi(
                                                                        pendaftaranRequestKonversi);
                                        DetailPendaftaranKonversi detailPendaftaranKonversi = new DetailPendaftaranKonversi();
                                        detailPendaftaranKonversi.setKrsDetail(krsDetail);
                                        detailPendaftaranKonversi
                                                        .setPendaftaranRequestKonversi(pendaftaranRequestKonversi);
                                        detailPendaftaranKonversi.setKeterangan("Pendaftran Konversi Jalur "
                                                        + filePendaftaranKonversi.getDetailKonfigurasiKonversi()
                                                                        .getNama());
                                        detailPendaftaranKonversi.setGradeLama(krsDetail.getGrade());
                                        detailPendaftaranKonversi
                                                        .setMatakuliah(krsDetail.getMatakuliahKurikulum()
                                                                        .getMatakuliah().getNamaMatakuliah());
                                        if (filePendaftaranKonversi.getDetailKonfigurasiKonversi()
                                                        .getNilaiGrade() == null) {
                                                Grade grade = gradeDao.findByNama(krsDetail.getGrade());
                                                int valueNilai = grade.getNilai()
                                                                + filePendaftaranKonversi.getDetailKonfigurasiKonversi()
                                                                                .getNilaiPlus();
                                                Grade maxGrade = gradeDao.findByNilai(gradeDao.maxNilai());
                                                String newGrade = valueNilai > maxGrade.getNilai() ? maxGrade.getNama()
                                                                : gradeDao.findByNilai(valueNilai).getNama();
                                                detailPendaftaranKonversi.setGradeBaru(newGrade);
                                        } else {
                                                detailPendaftaranKonversi
                                                                .setGradeBaru(filePendaftaranKonversi
                                                                                .getDetailKonfigurasiKonversi()
                                                                                .getNilaiGrade());
                                        }
                                        detailPendaftaranKonversiDao
                                                        .save(detailPendaftaranKonversi);
                                }
                        }
                }

                Integer totalSks = detailPendaftaranKonversiDao
                                .sumTotalSks(pendaftaranRequestKonversi);
                pendaftaranRequestKonversi.setSksDiambil(totalSks);
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);

                return "redirect:mahasiswa/detail";
        }

        @GetMapping("/request-konversi/mahasiswa/detail")
        public String detailRequestKonversiMahasiswa(Model model, Authentication authentication) {
                User user = currentUserService.currentUser(authentication);
                Mahasiswa mahasiswa = mahasiswaDao.findByUser(user);
                RequestKonversiTenggat requestKonversiTenggat = requestKonversiTenggatDao
                                .findByStatus(StatusRecord.AKTIF);
                LocalDateTime now = LocalDateTime.now().plusHours(7);
                LocalDateTime mulai = requestKonversiTenggat.getTanggalAwal().atStartOfDay();
                LocalDateTime selesai = requestKonversiTenggat.getTanggalAkhir().atStartOfDay();

                model.addAttribute("validasi", now.isAfter(mulai) && now.isBefore(selesai));

                List<PendaftaranRequestKonversi> pendaftaranRequestKonversiList = pendaftaranRequestKonversiDao
                                .findByMahasiswa(mahasiswa);
                model.addAttribute("listRequestKonversi", pendaftaranRequestKonversiList);
                return "requestKonversi/mahasiswa/detail";
        }

        @PostMapping("/api/approval-konversi/{id}")
        public ResponseEntity<BaseResponseDto> approvalKonversi(@PathVariable String id,
                        @RequestParam String approval) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                pendaftaranRequestKonversi.setStatus(Approval.valueOf(approval));
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);
                return ResponseEntity.status(HttpStatus.OK).body(BaseResponseDto.builder()
                                .responseCode("00").responseMessage("success").build());
        }

        @PostMapping("/api/rejected-konversi/{id}")
        public ResponseEntity<BaseResponseDto> rejectedKonversi(@PathVariable String id,
                        @RequestParam String approval) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                pendaftaranRequestKonversi.setStatus(Approval.valueOf(approval));
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);

                for (DetailPendaftaranKonversi detailPendaftaranKonversi : pendaftaranRequestKonversi
                                .getDetailPendaftaranKonversiList()) {
                        detailPendaftaranKonversi.setStatus(Approval.REJECTED);
                        detailPendaftaranKonversiDao.save(detailPendaftaranKonversi);
                }
                return ResponseEntity.status(HttpStatus.OK).body(BaseResponseDto.builder()
                                .responseCode("00").responseMessage("success").build());
        }

        @PostMapping("/request-konversi-delete/{id}")
        @Transactional
        public String deleteKonversi(@PathVariable String id) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                pendaftaranRequestKonversiDao.delete(pendaftaranRequestKonversi);
                detailPendaftaranKonversiDao.deleteDetailPendaftaranKonversiByPendaftaran(pendaftaranRequestKonversi);

                return "redirect:../request-konversi/mahasiswa/detail";
        }

        @PostMapping("/api/delete-matakuliah-konversi/{id}")
        public ResponseEntity<BaseResponseDto> rejectedKonversi(@PathVariable String id) {
                DetailPendaftaranKonversi detailPendaftaranKonversi = detailPendaftaranKonversiDao.findById(id).get();
                detailPendaftaranKonversiDao.delete(detailPendaftaranKonversi);
                Integer totalSks = detailPendaftaranKonversiDao
                                .sumTotalSks(detailPendaftaranKonversi.getPendaftaranRequestKonversi());
                PendaftaranRequestKonversi pendaftaranRequestKonversi = detailPendaftaranKonversi
                                .getPendaftaranRequestKonversi();
                pendaftaranRequestKonversi.setTotalSks(totalSks);
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);

                return ResponseEntity.status(HttpStatus.OK).body(BaseResponseDto.builder()
                                .responseCode("00").responseMessage("success").build());
        }

        @PostMapping("/api/approve-konversi/{id}")
        public ResponseEntity<BaseResponseDto> approveKonversi(@PathVariable String id) {
                PendaftaranRequestKonversi pendaftaranRequestKonversi = pendaftaranRequestKonversiDao.findById(id)
                                .get();
                pendaftaranRequestKonversi.setStatus(Approval.APPROVED);
                pendaftaranRequestKonversiDao.save(pendaftaranRequestKonversi);

                for (DetailPendaftaranKonversi detailPendaftaranKonversi : pendaftaranRequestKonversi
                                .getDetailPendaftaranKonversiList()) {
                        detailPendaftaranKonversi.setStatus(Approval.APPROVED);
                        detailPendaftaranKonversiDao.save(detailPendaftaranKonversi);
                        Grade grade = gradeDao.findByNama(detailPendaftaranKonversi.getGradeBaru());
                        KrsDetail krsDetail = detailPendaftaranKonversi.getKrsDetail();
                        krsDetail.setGrade(grade.getNama());
                        krsDetail.setBobot(grade.getBobot());
                        krsDetail.setNilaiAkhir(grade.getBawah());
                        krsDetail.setStatusKonversi(StatusRecord.REQUEST_KONVERSI);
                        krsDetailDao.save(krsDetail);
                }

                return ResponseEntity.status(HttpStatus.OK).body(BaseResponseDto.builder()
                                .responseCode("00").responseMessage("success").build());
        }

        @GetMapping("/upload/{id}/konversi/")
        public ResponseEntity<byte[]> tampilkanBukti(
                        @PathVariable(name = "id") PendaftaranRequestKonversi pendaftaranRequestKonversi)
                        throws Exception {
                String lokasiUpload = uploadFolder + File.separator
                                + pendaftaranRequestKonversi.getMahasiswa().getNim()
                                + File.separator
                                + pendaftaranRequestKonversi.getKonfigurasiKonversi().getNama()
                                + File.separator
                                + pendaftaranRequestKonversi.getFiles().get(0).getFile();
                String filename = pendaftaranRequestKonversi.getFiles().get(0).getFile();
                return fileService.tampilkanFile(filename, lokasiUpload);
        }

        @PostMapping("/request-konversi/update-tanggal")
        public String updateTanggalKonversi(@RequestParam String tanggalAwal,
                        @RequestParam String tanggalAkhir) {
                RequestKonversiTenggat requestKonversiTenggat = requestKonversiTenggatDao
                                .findByStatus(StatusRecord.AKTIF);
                requestKonversiTenggat.setTanggalAwal(LocalDate.parse(tanggalAwal));
                requestKonversiTenggat.setTanggalAkhir(LocalDate.parse(tanggalAkhir));
                requestKonversiTenggatDao.save(requestKonversiTenggat);

                return "redirect:admin";
        }
}
