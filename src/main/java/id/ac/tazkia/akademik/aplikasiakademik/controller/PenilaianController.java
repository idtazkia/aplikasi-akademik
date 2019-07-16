package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.ExcelFileConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.InputNilaiDto;
import id.ac.tazkia.akademik.aplikasiakademik.dto.NilaiDto;
import id.ac.tazkia.akademik.aplikasiakademik.dao.NilaiTugasDao;
import id.ac.tazkia.akademik.aplikasiakademik.dto.PenilaianDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import id.ac.tazkia.akademik.aplikasiakademik.service.CurrentUserService;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class PenilaianController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PenilaianController.class);

    @Autowired
    private JadwalDao jadwalDao;

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private TahunAkademikProdiDao tahunAkademikProdiDao;

    @Autowired
    private ProgramDao programDao;

    @Autowired
    private KrsDetailDao krsDetailDao;

    @Autowired
    private BobotTugasDao bobotTugasDao;

    @Autowired
    private NilaiTugasDao nilaiTugasDao;

    @Autowired
    private PresensiMahasiswaDao presensiMahasiswaDao;

    @Autowired
    private PresensiDosenDao presensiDosenDao;

    @Autowired
    private CurrentUserService currentUserService;

    @Value("${upload.excel}")
    private String uploadFolder;

    @Value("${spring.datasource.url}")
    private String urlDatabase;

    @Value("${spring.datasource.username}")
    private String usernameDb;

    @Value("${spring.datasource.password}")
    private String passwordDb;

    @ModelAttribute("tahunAkademik")
    public Iterable<TahunAkademikProdi> tahunAkademik() {
        return tahunAkademikProdiDao.findByStatusNotInOrderByTahunAkademikDesc(StatusRecord.HAPUS);
    }
    @ModelAttribute("program")
    public Iterable<Program> program() {
        return programDao.findByStatusNotIn(StatusRecord.HAPUS);
    }


    @GetMapping("/penilaian/list")
    public void listPenilaian(Model model,@PageableDefault(size = 5) Pageable page,@RequestParam(required = false) Program program,
                              @RequestParam(required = false) TahunAkademikProdi tahunAkademik){

        if (tahunAkademik == null) {
            TahunAkademik tahun = tahunAkademikDao.findByStatus(StatusRecord.AKTIF);
            Page<Jadwal> jadwal = jadwalDao.cariDosen(StatusRecord.AKTIF, tahun, page);
            model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademik(StatusRecord.AKTIF, tahun));
            if (jadwal != null || jadwal.isEmpty()) {
                model.addAttribute("dosen", jadwal);
            }
        }else {
            model.addAttribute("selectedTahun",tahunAkademik);
            model.addAttribute("selectedProgram",program);
            Page<Jadwal> jadwal = jadwalDao.cariDosen(StatusRecord.AKTIF, tahunAkademik.getTahunAkademik(), page);
            model.addAttribute("jadwal", jadwalDao.findByStatusAndTahunAkademikAndProdiAndProgram(StatusRecord.AKTIF, tahunAkademik.getTahunAkademik(),tahunAkademik.getProdi(),program));
            if (jadwal != null || jadwal.isEmpty()) {
                model.addAttribute("dosen", jadwal);
            }
        }
    }

    @GetMapping("/penilaian/bobot")
    public void bobotPenilaian(@RequestParam Jadwal jadwal,Model model){
        model.addAttribute("absensi", presensiDosenDao.findByStatusAndJadwal(StatusRecord.AKTIF,jadwal).size());
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("jumlahMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF).size());
        model.addAttribute("bobot",bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF));
    }

    @PostMapping("/penilaian/bobot")
    public String saveBobot(@ModelAttribute @Valid Jadwal jadwal, RedirectAttributes attributes ,
                            @RequestParam String jamMulai,
                            @RequestParam String jamSelesai){
        BigDecimal totalBobot = jadwal.getBobotPresensi().add(jadwal.getBobotTugas()).add(jadwal.getBobotUas()).add(jadwal.getBobotUts());
        LocalTime mulai = LocalTime.parse(jamMulai,
                DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime selesai = LocalTime.parse(jamSelesai,
                DateTimeFormatter.ofPattern("HH:mm:ss"));
        if (totalBobot.toBigInteger().intValueExact() > 100){
            attributes.addFlashAttribute("lebih", "Melebihi Batas");
        }else {
            jadwal.setJamSelesai(mulai);
            jadwal.setJamMulai(selesai);
            jadwalDao.save(jadwal);
            System.out.println("cukup");
        }
        return "redirect:bobot?jadwal="+jadwal.getId();
    }

    @PostMapping("/penilaian/tugas")
    public String tambahTugas(@ModelAttribute @Valid BobotTugas bobotTugas){
        bobotTugasDao.save(bobotTugas);
        return "redirect:bobot?jadwal="+bobotTugas.getJadwal().getId();
    }

    @PostMapping("/penilaian/delete")
    public String deleteBobot(@RequestParam String bobot){
        BobotTugas bobotTugas = bobotTugasDao.findById(bobot).get();
        bobotTugas.setStatus(StatusRecord.HAPUS);
        bobotTugasDao.save(bobotTugas);
        return "redirect:bobot?jadwal="+bobotTugas.getJadwal().getId();
    }


    @GetMapping("/penilaian/nilai")
    public String nilaiPenilaian(@RequestParam Jadwal jadwal, Model model, RedirectAttributes attributes){
        BigDecimal totalBobot = jadwal.getBobotPresensi().add(jadwal.getBobotTugas()).add(jadwal.getBobotUas()).add(jadwal.getBobotUts());
        List<BobotTugas> bobot = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
        BigDecimal sum = bobot.stream()
                .map(BobotTugas::getBobot)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        System.out.println(sum.toBigInteger().intValueExact());
        if (bobot.size() != 0 || !bobot.isEmpty()) {
            if (sum.toBigInteger().intValueExact() < 100) {
                attributes.addFlashAttribute("tidakvalid", "Melebihi Batas");
                System.out.println("gabisa");
                return "redirect:bobot?jadwal=" + jadwal.getId();
            }
        }

        if (totalBobot.toBigInteger().intValueExact() < 100) {
            attributes.addFlashAttribute("tidakvalid", "Melebihi Batas");
            System.out.println("gabisa");
            return "redirect:bobot?jadwal="+jadwal.getId();
        }

        model.addAttribute("absensi", presensiDosenDao.findByStatusAndJadwal(StatusRecord.AKTIF,jadwal).size());
        model.addAttribute("jumlahMahasiswa", krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF).size());
        List<NilaiDto> nim = new ArrayList<>();
        List<NilaiDto> id = new ArrayList<>();
        List<PenilaianDto> penilaianDtos = new ArrayList<>();
        model.addAttribute("jadwal", jadwal);
        model.addAttribute("bobotTugas", bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF));
        model.addAttribute("mahasiswa", penilaianDtos);
        model.addAttribute("nilai", nilaiTugasDao.findByStatusAndKrsDetailJadwal(StatusRecord.AKTIF,jadwal));
        model.addAttribute("pres", presensiMahasiswaDao.findByKrsDetailJadwalAndStatus(jadwal,StatusRecord.AKTIF));
        for (KrsDetail krsDetail : krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF)){
            int presensiMahasiswa = presensiMahasiswaDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF).size();
            int presensiDosen = presensiDosenDao.findByStatusAndJadwal(StatusRecord.AKTIF,krsDetail.getJadwal()).size();
            PenilaianDto penilaianDto = new PenilaianDto();
            penilaianDto.setKrsDetail(krsDetail);
            penilaianDto.setId(krsDetail.getId());
            penilaianDto.setPresensiDosen(presensiDosen);
            penilaianDto.setAbsensiMahasiswa(presensiMahasiswa);
            penilaianDtos.add(penilaianDto);
        }

        List<KrsDetail> kd = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF);
        for (KrsDetail krsDetail : kd){
            NilaiDto r = new NilaiDto();
            r.setId(krsDetail.getMahasiswa().getNim());
            r.setNilai(String.valueOf(krsDetail.getNilaiUts()));
            nim.add(r);
        }
        List<BobotTugas> bobotTugas = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);
        for (BobotTugas bt : bobotTugas){
            NilaiDto r = new NilaiDto();
            r.setId(bt.getId());
            id.add(r);
        }
        model.addAttribute("jsMahasiswa", nim);
        model.addAttribute("bobot", id);

        return "/penilaian/nilai";
    }

    @GetMapping("/penilaian/edit")
    public void editBobot(@RequestParam BobotTugas bobot, Model model){
        model.addAttribute("bobotTugas", bobot);

    }

    @PostMapping("/penilaian/edit")
    public String prosesEdit(@ModelAttribute @Valid BobotTugas bobotTugas){
        bobotTugasDao.save(bobotTugas);
        return "redirect:bobot?jadwal="+bobotTugas.getJadwal().getId();
    }

    @PostMapping(value = "/penilaian/nilai")
    @ResponseBody @ResponseStatus(HttpStatus.OK)
    public String simpanNilai(@RequestBody @Valid InputNilaiDto in) throws Exception   {
        if (in == null){

        }else {

            if (in.getNilai().isEmpty() || in.getNilai() == "") {
                if (in.getNilai().isEmpty() || in.getNilai() == "" && in.getUts().isEmpty() || in.getUts() == "" && in.getUas() == null || in.getUas() == "") {
                }
                if (in == null && in.getUts().isEmpty() || in.getUts() == "" || in.getUts() == null) {

                } else {
                    KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                    krsDetail.setNilaiUts(new BigDecimal(in.getUts()).multiply(krsDetail.getJadwal().getBobotUts()).divide(new BigDecimal(100)));
                    krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts()).add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUas()));
                    krsDetailDao.save(krsDetail);
                    return "redirect:penilaian/nilai?jadwal=" + krsDetail.getJadwal().getId();
                }

                if (in.getUas() == null || in.getUas() == "") {

                } else {
                    KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                    krsDetail.setNilaiUas(new BigDecimal(in.getUas()).multiply(krsDetail.getJadwal().getBobotUas()).divide(new BigDecimal(100)));
                    krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts()).add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUas()));
                    krsDetailDao.save(krsDetail);
                }
            } else {

                BobotTugas bobotTugas = bobotTugasDao.findById(in.getTugas()).get();
                KrsDetail krsDetail = krsDetailDao.findById(in.getKrs()).get();
                NilaiTugas validasi = nilaiTugasDao.findByStatusAndBobotTugasAndKrsDetail(StatusRecord.AKTIF, bobotTugas, krsDetail);


                if (validasi == null) {
                    NilaiTugas nilaiTugas = new NilaiTugas();
                    nilaiTugas.setBobotTugas(bobotTugas);
                    nilaiTugas.setKrsDetail(krsDetail);
                    BigDecimal nilai = bobotTugas.getBobot().multiply(new BigDecimal(in.getNilai()).divide(new BigDecimal(100)));
                    nilaiTugas.setNilaiAkhir(nilai);
                    nilaiTugas.setNilai(new BigDecimal(in.getNilai()));
                    nilaiTugasDao.save(nilaiTugas);
                    List<NilaiTugas> nilaiAkhir = nilaiTugasDao.findByStatusAndKrsDetail(StatusRecord.AKTIF, krsDetail);
                    int presensiMahasiswa = presensiMahasiswaDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF).size();
                    BigDecimal sum = nilaiAkhir.stream()
                            .map(NilaiTugas::getNilaiAkhir)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    System.out.println(sum);
                    KrsDetail kd = krsDetailDao.findById(nilaiTugas.getKrsDetail().getId()).get();
                    kd.setNilaiPresensi(BigDecimal.valueOf(presensiMahasiswa).multiply(kd.getJadwal().getBobotPresensi()).divide(new BigDecimal(100)));
                    kd.setNilaiTugas(sum.multiply(kd.getJadwal().getBobotTugas()).divide(new BigDecimal(100)));
                    krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts()).add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUas()));
                    krsDetailDao.save(kd);
                    return "redirect:penilaian/nilai?jadwal=" + kd.getJadwal().getId();

                } else {
                    BigDecimal nilai = bobotTugas.getBobot().multiply(new BigDecimal(in.getNilai()).divide(new BigDecimal(100)));
                    validasi.setNilaiAkhir(nilai);
                    validasi.setNilai(new BigDecimal(in.getNilai()));
                    nilaiTugasDao.save(validasi);
                    List<NilaiTugas> nilaiAkhir = nilaiTugasDao.findByStatusAndKrsDetail(StatusRecord.AKTIF, krsDetail);
                    BigDecimal sum = nilaiAkhir.stream()
                            .map(NilaiTugas::getNilaiAkhir)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    System.out.println(sum);
                    KrsDetail kd = krsDetailDao.findById(validasi.getKrsDetail().getId()).get();
                    int presensiMahasiswa = presensiMahasiswaDao.findByKrsDetailAndStatus(krsDetail,StatusRecord.AKTIF).size();
                    kd.setNilaiTugas(sum.multiply(kd.getJadwal().getBobotTugas()).divide(new BigDecimal(100)));
                    kd.setNilaiPresensi(BigDecimal.valueOf(presensiMahasiswa).multiply(kd.getJadwal().getBobotPresensi()).divide(new BigDecimal(100)));
                    krsDetail.setNilaiAkhir(krsDetail.getNilaiTugas().add(krsDetail.getNilaiUts()).add(krsDetail.getNilaiPresensi()).add(krsDetail.getNilaiUas()));
                    krsDetailDao.save(kd);
                    return "redirect:penilaian/nilai?jadwal=" + kd.getJadwal().getId();
                }

            }
        }


        return null;
    }

    @GetMapping("/api/nilai")
    @ResponseBody
    public KrsDetail findByJadwal(@RequestParam(required = false) KrsDetail krsDetail ,Model model){
        model.addAttribute("otomatisNilai", krsDetail);
        return krsDetail;
    }

    @PostMapping("/penilaian/upload")
    public String postTest(MultipartFile file,Authentication authentication)throws Exception{
        User user = currentUserService.currentUser(authentication);

        String namaFile = file.getName();
        String jenisFile = file.getContentType();
        String namaAsli = file.getOriginalFilename();
        Long ukuran = file.getSize();

        System.out.println("Nama File : {}" + namaFile);
        System.out.println("Jenis File : {}" + jenisFile);
        System.out.println("Nama Asli File : {}" + namaAsli);
        System.out.println("Ukuran File : {}"+ ukuran);

//        memisahkan extensi
        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder + File.separator + user.getId();
        LOGGER.debug("Lokasi upload : {}", lokasiUpload);
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        file.transferTo(tujuan);
        LOGGER.debug("File sudah dicopy ke : {}", tujuan.getAbsolutePath());

        String fileName= tujuan.getAbsolutePath();
        Vector dataHolder=read(fileName);
        saveToDatabase(dataHolder);



        return "redirect:list";
    }

    public static Vector read(String fileName)    {
        Vector cellVectorHolder = new Vector();
        try{
            FileInputStream myInput = new FileInputStream(fileName);
            DataFormatter dataFormatter = new DataFormatter();
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myInput);
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator rowIter = mySheet.rowIterator();
            FormulaEvaluator evaluator = myWorkBook.getCreationHelper().createFormulaEvaluator();
            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator cellIter = myRow.cellIterator();
                //Vector cellStoreVector=new Vector();
                List list = new ArrayList();
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    list.add(myCell);
                }
                cellVectorHolder.addElement(list);
            }
        }catch (Exception e){e.printStackTrace(); }
        return cellVectorHolder;
    }

    public void saveToDatabase(Vector dataHolder) {
        String id = "";
        String idUser = "";
        String password = "";

        for (Iterator iterator = dataHolder.iterator(); iterator.hasNext(); ) {
            List list = (List) iterator.next();
            System.out.println(list);
            id = list.get(0).toString();
            idUser = list.get(4).toString();
            password = list.get(2).toString();

            try {
                Class.forName("com.mysql.jdbc.Driver").newInstance();
                Connection con = DriverManager.getConnection(urlDatabase, usernameDb, passwordDb);
                System.out.println("connection made...");
                PreparedStatement stmt = con.prepareStatement("INSERT INTO susah(column1,column2) VALUES(?,?)");
                stmt.setString(1, id);
                stmt.setString(2, idUser);
                stmt.executeUpdate();

                System.out.println("Data is inserted");
                stmt.close();
                con.close();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    @GetMapping("/nilai/excel")
    public void downloadExcel (@RequestParam(required = false) String id, HttpServletResponse response) throws IOException {

        List<String> staticColumn1 = new ArrayList<>();
        List<String> staticColumn2 = new ArrayList<>();
        List<String> dynamicColumn = new ArrayList<>();
        List<String> uts = new ArrayList<>();
        List<String> uas = new ArrayList<>();
        List<String> total = new ArrayList<>();
        List<String> grade = new ArrayList<>();

        staticColumn1.add("No.   ");
        staticColumn1.add("NIM             ");
        staticColumn1.add("NAMA                                                   ");

        Jadwal jadwal = jadwalDao.findById(id).get();
        List<KrsDetail> krsDetail = krsDetailDao.findByJadwalAndStatusOrderByMahasiswaNamaAsc(jadwal,StatusRecord.AKTIF);
        List<BobotTugas> bobotTugas = bobotTugasDao.findByJadwalAndStatus(jadwal,StatusRecord.AKTIF);

        for (BobotTugas bt : bobotTugas){
            dynamicColumn.add("Nilai " + bt.getNamaTugas());
            dynamicColumn.add("Total " + bt.getNamaTugas());

        }
        uts.add("Total UTS");
        uas.add(" Total UAS");
        staticColumn2.add("Nilai UTS");
        staticColumn2.add("Total UTS");
        staticColumn2.add("Nilai UAS");
        staticColumn2.add(" Total UAS");
        staticColumn2.add("Total");
        total.add("Total");
        staticColumn2.add("Grade");



        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("penilaian");
        sheet.autoSizeColumn(9);


        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.getIndex());


        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);

        Integer cellNum = 0;

        for (String header : staticColumn1) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellValue(header);
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(cellNum);
            cellNum++;
        }

        for (String header : dynamicColumn) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellValue(header);
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(cellNum);
            cellNum++;
        }

        for (String header : staticColumn2) {
            Cell cell = headerRow.createCell(cellNum);
            cell.setCellValue(header);
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(cellNum);
            cellNum++;
        }


        int rowNum = 1 ;
        for (KrsDetail kd : krsDetail) {
            int kolom = 0 ;

            Row row = sheet.createRow(rowNum);
            row.createCell(kolom++).setCellValue(kolom);
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNim());
            row.createCell(kolom++).setCellValue(kd.getMahasiswa().getNama());


            for (BobotTugas bt : bobotTugas) {
                row.createCell(kolom++); // buat ngisi nilai
                Cell cellNilaiBobot = row.createCell(kolom);
                cellNilaiBobot.setCellType(CellType.FORMULA);
                cellNilaiBobot.setCellFormula(ExcelFileConstants.COLUMN_NAMES[kolom - 1]+(rowNum+1)
                +"*"+bt.getBobot().toPlainString()+"/"+new Integer(100)+"*"+kd.getJadwal().getBobotTugas()+"/"+new Integer(100));
                kolom++;
            }

            for (String s : uts) {
                row.createCell(kolom++);
                Cell cellNilaiBobot = row.createCell(kolom);
                cellNilaiBobot.setCellType(CellType.FORMULA);
                cellNilaiBobot.setCellFormula(ExcelFileConstants.COLUMN_NAMES[kolom-1]+(rowNum+1)
                        +"*"+kd.getJadwal().getBobotUts().toPlainString()+"/"+new Integer(100));
                kolom++;


            }

            for (String s : uas) {
                row.createCell(kolom++);
                Cell cellNilaiBobot = row.createCell(kolom);
                cellNilaiBobot.setCellType(CellType.FORMULA);
                cellNilaiBobot.setCellFormula(ExcelFileConstants.COLUMN_NAMES[kolom-1]+(rowNum+1)
                        +"*"+kd.getJadwal().getBobotUas().toPlainString()+"/"+new Integer(100));
                kolom++;

            }

            int totalColumn = bobotTugas.size()+2;
            for (String s : total){
                Cell cellNilaiBobot = row.createCell(kolom);
                cellNilaiBobot.setCellType(CellType.FORMULA);
                cellNilaiBobot.setCellFormula("SUM("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()]+(rowNum+1)+":" + ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()+totalColumn-1]+(rowNum+1)+")");
                kolom++;
            }

            row.createCell(kolom++).setCellFormula("IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(85) + ",\"A\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(80) + ",\"A-\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(75) + ",\"B+\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(70) + ",\"B\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(65) + ",\"B-\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(60) + ",\"C+\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(55) + ",\"C\"" + ",IF("+ExcelFileConstants.COLUMN_NAMES[sheet.getRow(0).getLastCellNum()-2]+(rowNum+1)+">="+new Integer(50) + ",\"D\""+",\"E\""+")"+")" +")"+")" +")"+")" +")"+")") ;



            for (int i = 4; i<sheet.getRow(0).getLastCellNum()-2; i+= 2){

                Cell cellNilaiBobot = row.createCell(kolom);
                cellNilaiBobot.setCellType(CellType.FORMULA);
                cellNilaiBobot.setCellFormula("SUM("+ExcelFileConstants.COLUMN_NAMES[i]+(rowNum+1)+")");
                sheet.setColumnHidden(kolom,true);
                kolom++;
            }


            rowNum++;
        }





        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-disposition", "attachment; filename=Penilaian.xlsx");
        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
