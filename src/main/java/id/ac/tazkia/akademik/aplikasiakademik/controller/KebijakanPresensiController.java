package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.JadwalDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class KebijakanPresensiController {

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private SesiKuliahDao sesiKuliahDao;

    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private JadwalDosenDao jadwalDosenDao;

    @Autowired
    private DosenDao dosenDao;

    @Autowired
    private KelasDao kelasDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private KaryawanDao karyawanDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    CurrentUserService currentUserService;

    @Autowired
    private RuanganDao ruanganDao;

    @Autowired
    private HariDao hariDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @ModelAttribute("dosen")
    public Iterable<Dosen> dosen() {
        return dosenDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("ruangan")
    public Iterable<Ruangan> ruangan() {
        return ruanganDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("kelas")
    public Iterable<Kelas> kelas() {
        return kelasDao.findByStatusNotIn(StatusRecord.HAPUS);
    }

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademikProdi> tahunAkademik() {
        return tahunAkademikProdiDao.findByStatusNotInOrderByTahunAkademikDesc(StatusRecord.HAPUS);
    }

    @ModelAttribute("hari")
    public Iterable<Hari> hari() {
        return hariDao.findAll();
    }

    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }


    @GetMapping("/kebijakanpresensi/list")
    public void daftarKebijakanPresensi(Model model, @RequestParam(required = false) Program program,
                                        @RequestParam(required = false) TahunAkademikProdi tahunAkademik,
                                        @RequestParam(required = false) Hari hari){
        model.addAttribute("selectedTahun",tahunAkademik);
        model.addAttribute("selectedHari", hari);
        model.addAttribute("selectedProgram",program);

        if (program != null && tahunAkademik != null && hari != null){
            model.addAttribute("jadwal", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,hari,program));
        }


        if (program != null && tahunAkademik != null && hari == null){
            model.addAttribute("minggu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"0",program));
            model.addAttribute("senin", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"1",program));
            model.addAttribute("selasa", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"2",program));
            model.addAttribute("rabu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"3",program));
            model.addAttribute("kamis", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"4",program));
            model.addAttribute("jumat", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"5",program));
            model.addAttribute("sabtu", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariIdAndProgramAndHariNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik,"6",program));
            model.addAttribute("ploting", jadwalDao.findByStatusNotInAndProdiAndTahunAkademikProdiAndHariNullAndJamMulaiNullAndJamSelesaiNullAndKelasNotNull(StatusRecord.HAPUS,tahunAkademik.getProdi(),tahunAkademik));
        }

    }

    @GetMapping("/kebijakanpresensi/listdosen")
    public void daftarKebijakanPresensi(Model model, Authentication authentication){
        User user = currentUserService.currentUser(authentication);
        Karyawan karyawan = karyawanDao.findByIdUser(user);
        Dosen dosen = dosenDao.findByKaryawan(karyawan);

        TahunAkademik tahunAkademik = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
        Iterable<JadwalDosen> jadwal = jadwalDosenDao.findByJadwalStatusNotInAndJadwalTahunAkademikAndDosenAndJadwalHariNotNullAndJadwalKelasNotNull(StatusRecord.HAPUS, tahunAkademik,dosen);
model.addAttribute("jadwal", jadwal);

    }

    @GetMapping("/kebijakanpresensi/detail")
    public void  formKebijakanPresensi(Model model, @RequestParam Jadwal jadwal, Pageable page){
        List<SesiKuliah> sesiKuliah = sesiKuliahDao.findByJadwal(jadwal);

        if (sesiKuliah != null) {
            List<JadwalDto> detail = new ArrayList<>();
            for (SesiKuliah sk : sesiKuliah) {
                JadwalDto jadwalDto = new JadwalDto();
                jadwalDto.setJadwal(sk.getJadwal());
                jadwalDto.setBeritaAcara(sk.getBeritaAcara());
                jadwalDto.setPresensiDosen(sk.getPresensiDosen());
                jadwalDto.setId(sk.getId());
                LocalDateTime jamMasuk = sk.getWaktuMulai();
                LocalDateTime jamSelesai = sk.getWaktuSelesai();
                DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yy hh:mm:ss");
                jadwalDto.setWaktuMulai(jamMasuk.format(format));
                jadwalDto.setWaktuSelesai(jamSelesai.format(format));
                detail.add(jadwalDto);

            }
            model.addAttribute("detail", detail);
        }
        model.addAttribute("dosenUtama", jadwal.getDosen());
//        model.addAttribute("teamTeaching", jadwal.getDosens());
        model.addAttribute("jadwal", jadwal);
    }

    @PostMapping("/kebijakanpresensi/detail")
    public String tambahPresensi(@ModelAttribute @Valid JadwalDto jadwalDto){
        PresensiDosen presensiDosen = new PresensiDosen();
        presensiDosen.setDosen(jadwalDto.getDosen());
        presensiDosen.setJadwal(jadwalDto.getJadwal());
        presensiDosen.setStatusPresensi(StatusPresensi.HADIR);
        presensiDosen.setTahunAkademik(jadwalDto.getTahunAkademik());
        presensiDosen.setWaktuMasuk(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));
        presensiDosen.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamSelesai()));
        presensiDosenDao.save(presensiDosen);

        SesiKuliah sesiKuliah = new SesiKuliah();
        sesiKuliah.setBeritaAcara(jadwalDto.getBeritaAcara());
        sesiKuliah.setJadwal(jadwalDto.getJadwal());
        sesiKuliah.setPresensiDosen(presensiDosen);
        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMulai()));
        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamSelesai()));
        sesiKuliahDao.save(sesiKuliah);

        return "redirect:detail?jadwal=" + jadwalDto.getJadwal().getId();

    }

    @GetMapping("/kebijakanpresensi/mahasiswa")
    public String mahasiswaKebijakanPresensi(@RequestParam(name = "id",value = "id") SesiKuliah sesiKuliah, Model model){
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliah(sesiKuliah);
        if (presensiMahasiswa == null || presensiMahasiswa.isEmpty()){
            model.addAttribute("daftarMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(sesiKuliah.getJadwal(),StatusRecord.AKTIF));
            model.addAttribute("jadwal", sesiKuliah.getJadwal().getId());
            model.addAttribute("sesi",sesiKuliah.getId());
            model.addAttribute("statusPresensi", StatusPresensi.values());

            return "kebijakanpresensi/mahasiswa";
        }else {
            return "redirect:presensi?id=" + sesiKuliah.getId();
        }
    }

    @PostMapping("/kebijakanpresensi/mahasiswa")
    public String proses(@RequestParam String jadwal,@RequestParam String sesi, HttpServletRequest request){
        Jadwal j = jadwalDao.findById(jadwal).get();
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();
        for (KrsDetail krsDetail : krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,StatusRecord.AKTIF)){
            String pilihan = request.getParameter(krsDetail.getMahasiswa().getNim()+"nim");
            if (pilihan == null){
                System.out.println("tidak ada");
            }else {
                PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
                presensiMahasiswa.setMahasiswa(krsDetail.getMahasiswa());
                StatusPresensi statusPresensi = StatusPresensi.valueOf(pilihan);
                presensiMahasiswa.setStatusPresensi(statusPresensi);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setKrsDetail(krsDetail);
                presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now());
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswaDao.save(presensiMahasiswa);
                System.out.println(presensiMahasiswa.getId());

            }

        }
        return "redirect:detail?jadwal="+j.getId();
    }

    @GetMapping("/kebijakanpresensi/presensi")
    public void dosenKebijakanPresensi(@RequestParam(name = "id",value = "id") SesiKuliah sesiKuliah, Model model){
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliah(sesiKuliah);

        Map<String, String> statusPresensi = new HashMap<>();
        for(PresensiMahasiswa pm : presensiMahasiswa){
            statusPresensi.put(pm.getId(), pm.getStatusPresensi().toString());
        }

        model.addAttribute("statusPresensi", StatusPresensi.values());
        model.addAttribute("status", statusPresensi);
        model.addAttribute("presensi", presensiMahasiswa);
        model.addAttribute("jadwal", sesiKuliah.getJadwal().getId());
        model.addAttribute("sesi",sesiKuliah.getId());
    }

    @PostMapping("/kebijakanpresensi/presensi")
    public String update(@RequestParam String jadwal,@RequestParam String sesi,HttpServletRequest request){
        Jadwal j = jadwalDao.findById(jadwal).get();
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();

        for (PresensiMahasiswa presensiMahasiswa : presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF)){
            String pilihan = request.getParameter(presensiMahasiswa.getMahasiswa().getNim() + "nim");
            if (pilihan == null || pilihan.isEmpty()){
                System.out.println("gaada");
            }else {
                presensiMahasiswa.setMahasiswa(presensiMahasiswa.getMahasiswa());
                StatusPresensi statusPresensi = StatusPresensi.valueOf(pilihan);
                presensiMahasiswa.setStatusPresensi(statusPresensi);
                presensiMahasiswa.setCatatan("Manual");
                presensiMahasiswa.setKrsDetail(presensiMahasiswa.getKrsDetail());
                presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),j.getJamSelesai()));
                presensiMahasiswa.setWaktuMasuk(LocalDateTime.now());
                presensiMahasiswa.setSesiKuliah(sesiKuliah);
                presensiMahasiswaDao.save(presensiMahasiswa);
                System.out.println(presensiMahasiswa.getId());
            }

        }

        return "redirect:detail?jadwal="+j.getId();
    }

    @PostMapping("/presensi/save")
    public String savePresensi(@RequestParam(required = false) String sesi, @RequestParam(required = false) StatusPresensi statusPresensi){
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();
        List<PresensiMahasiswa> presensiMahasiswa = presensiMahasiswaDao.findBySesiKuliahAndStatus(sesiKuliah,StatusRecord.AKTIF);
        for (PresensiMahasiswa pm : presensiMahasiswa){
            pm.setStatusPresensi(statusPresensi);
            presensiMahasiswaDao.save(pm);
        }
        return "redirect:/kebijakanpresensi/detail?jadwal="+sesiKuliah.getJadwal().getId();
    }

    @PostMapping("/mahasiswa/save")
    public String saveMahasiswa(@RequestParam(required = false) String sesi, @RequestParam(required = false) StatusPresensi statusPresensi){
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(sesi).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(sesiKuliah.getJadwal(),StatusRecord.AKTIF);
        for (KrsDetail kd : krsDetail){
            PresensiMahasiswa presensiMahasiswa = new PresensiMahasiswa();
            presensiMahasiswa.setMahasiswa(kd.getMahasiswa());
            presensiMahasiswa.setStatusPresensi(statusPresensi);
            presensiMahasiswa.setCatatan("Manual");
            presensiMahasiswa.setKrsDetail(kd);
            presensiMahasiswa.setWaktuKeluar(LocalDateTime.of(LocalDate.now(),kd.getJadwal().getJamSelesai()));
            presensiMahasiswa.setWaktuMasuk(LocalDateTime.now());
            presensiMahasiswa.setSesiKuliah(sesiKuliah);
            presensiMahasiswaDao.save(presensiMahasiswa);
        }
        return "redirect:/kebijakanpresensi/list";
    }

    @GetMapping("/kebijakanpresensi/detailedit")
    public void editDetail(Model model, @RequestParam String id){

        SesiKuliah sk = sesiKuliahDao.findById(id).get();
        JadwalDto jadwalDto = new JadwalDto();
        jadwalDto.setId(sk.getId());
        jadwalDto.setJadwal(sk.getJadwal());
        jadwalDto.setBeritaAcara(sk.getBeritaAcara());
        jadwalDto.setDosen(sk.getPresensiDosen().getDosen());
        jadwalDto.setPresensiDosen(sk.getPresensiDosen());
        jadwalDto.setTanggal(sk.getWaktuMulai().toLocalDate());
        jadwalDto.setId(sk.getId());
        jadwalDto.setJamMulai(sk.getWaktuMulai().toLocalTime());
        jadwalDto.setJamSelesai(sk.getWaktuSelesai().toLocalTime());
//        jadwalDto.setDosens(sk.getJadwal().getDosens());



        model.addAttribute("sesi", jadwalDto);

    }

    @PostMapping("/kebijakanpresensi/detailedit")
    public String updateDetail(@Valid @ModelAttribute JadwalDto jadwalDto){
        SesiKuliah sesiKuliah = sesiKuliahDao.findById(jadwalDto.getId()).get();
        sesiKuliah.setBeritaAcara(jadwalDto.getBeritaAcara());
        sesiKuliah.setWaktuMulai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamMasuk().toLocalTime()));
        sesiKuliah.setWaktuSelesai(LocalDateTime.of(jadwalDto.getTanggal(),jadwalDto.getJamKeluar().toLocalTime()));
        sesiKuliah.getPresensiDosen().setDosen(jadwalDto.getDosen());
        sesiKuliahDao.save(sesiKuliah);
        return "redirect:detail";
    }


    @GetMapping("/data/absen")
    public void dataAbsen(HttpServletResponse response,String jadwal) throws Exception {

        String[] columns = {"No","NIM", "Nama                                        ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data-Absen");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(11);

        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerCellStyle);
        }

        Jadwal j = jadwalDao.findById(jadwal).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(j,StatusRecord.AKTIF);


        sheet.createRow(0).createCell(3).setCellValue("            ATTENDANCE LIST OF SECOND SEMESTER");
        sheet.createRow(1).createCell(3).setCellValue("    TAZKIA UNIVERSITY COLLEGE OF ISLAMIC ECONOMICS");
        sheet.createRow(2).createCell(3).setCellValue("                    ACADEMIC YEAR 2018/2019");

        int rowInfo = 5 ;
        Row rowi1 = sheet.createRow(rowInfo);
        rowi1.createCell(0).setCellValue("Departement/ Group : ");
        rowi1.createCell(1).setCellValue(j.getProdi().getNamaProdi());



        int rowInfo2 = 6 ;
        Row rowi2 = sheet.createRow(rowInfo2);
        rowi2.createCell(0).setCellValue("Subject : ");
        rowi2.createCell(1).setCellValue(j.getMatakuliahKurikulum().getMatakuliah().getNamaMatakuliah());


        int rowInfo3 = 7 ;
        Row rowi3 = sheet.createRow(rowInfo3);
        rowi3.createCell(0).setCellValue("Lecture : ");
        rowi3.createCell(1).setCellValue(j.getDosen().getKaryawan().getNamaKaryawan());




        int rowNum = 12 ;
        for (KrsDetail kd : krsDetail) {
            int kolom = 0 ;
            Row row = sheet.createRow(rowNum);
            row.createCell(kolom++).setCellValue(kolom);
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNim());
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNama());

            rowNum++;


        }


        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=Data-Absen.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();


    }
}
