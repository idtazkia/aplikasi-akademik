package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MatkulDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
public class MataKuliahController {

    @Autowired
    private KurikulumDao kurikulumDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private MataKuliahDao mataKuliahDao;

    @Autowired
    private KonsentrasiDao konsentrasiDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private KelasDao kelasDao;

    @Value("${upload.silabus}")
    private String uploadFolder;

    @GetMapping("/api/matakuliah")
    @ResponseBody
    public Page<Matakuliah> cariData(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return mataKuliahDao.findAll(page);
        }
        return mataKuliahDao.findByNamaMatakuliahContainingIgnoreCase(search, page);

    }

    @GetMapping("/api/matkul")
    @ResponseBody
    public Matakuliah cariMatkul(@RequestParam(required = false) String id){
        if (id == null || id.isEmpty()){
            System.out.println("gaada");
        }

        return mataKuliahDao.findById(id).get();

    }

    @GetMapping("/api/prodi")
    @ResponseBody
    public Page<Prodi> cariProdi(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return prodiDao.findByStatus(StatusRecord.AKTIF,page);
        }
        return prodiDao.findByStatusAndNamaProdiContainingIgnoreCaseOrderByNamaProdi(StatusRecord.AKTIF,search, page);

    }

    @GetMapping({"/api/kurikulum"})
    @ResponseBody
    public List<Kurikulum> findByProdiAndName(@RequestParam(required = false) String namaProdi, @RequestParam String search,Pageable page){
        if(!StringUtils.hasText(search)) {
            return null;
        }
        return kurikulumDao.findByProdiAndNamaKurikulumContainingIgnoreCaseOrderByNamaKurikulum(prodiDao.findById(namaProdi).get(), search);
    }

    @GetMapping({"/api/kelas"})
    @ResponseBody
    public Page<Kelas> cariKelas(@RequestParam(required = false) String idProdi, @RequestParam String search,Pageable page){
        if(!StringUtils.hasText(search)) {
            return null;
        }
        return kelasDao.findByStatusAndIdProdiAndNamaKelasContainingIgnoreCaseOrderByNamaKelas(StatusRecord.AKTIF,prodiDao.findById(idProdi).get(),search,page);

    }

    @GetMapping("/matakuliah/list")
    public void  listMatakuliah(Model model, @RequestParam(required = false) Prodi prodi,
                                @RequestParam(required = false) Kurikulum kurikulum){
        model.addAttribute("listProdi",prodiDao.findByStatusNotIn(StatusRecord.HAPUS));
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(StatusRecord.HAPUS));

        if (prodi != null && kurikulum != null){
            model.addAttribute("selected",prodi);
            model.addAttribute("kurikulumSelected",kurikulum);
            model.addAttribute("satu",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,1));
            model.addAttribute("dua",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,2));
            model.addAttribute("tiga",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,3));
            model.addAttribute("empat",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,4));
            model.addAttribute("lima",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,5));
            model.addAttribute("enam",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,6));
            model.addAttribute("tujuh",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,7));
            model.addAttribute("delapan",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,8));
            model.addAttribute("sembilan",matakuliahKurikulumDao.findByStatusNotInAndKurikulumAndKurikulumProdiAndSemester(StatusRecord.HAPUS,kurikulum,prodi,9));
        }
    }

    @GetMapping("/matakuliah/form")
    public void  formMataKuliah(@RequestParam(required = false) String prodi,@RequestParam(required = false) String kurikulum,
                                Pageable pageable, @RequestParam(required = false)String id,Model model){
        model.addAttribute("prodi",prodiDao.findById(prodi).get());
        model.addAttribute("kurikulum",kurikulumDao.findById(kurikulum).get());
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(StatusRecord.HAPUS));

        Iterable<Konsentrasi> konsentrasi = konsentrasiDao.findByIdProdiAndStatus(prodiDao.findById(prodi).get(),StatusRecord.AKTIF);
        model.addAttribute("konsentrasi", konsentrasi);
        System.out.println(konsentrasi);
        Page<Matakuliah> matakuliah = mataKuliahDao.findByStatus(StatusRecord.AKTIF,pageable);
        model.addAttribute("listMatkul", matakuliah);

        model.addAttribute("matkul", new MatkulDto());

        if (id != null && !id.isEmpty()) {
            MatakuliahKurikulum matakuliahKurikulum = matakuliahKurikulumDao.findById(id).get();
            if (matakuliahKurikulum != null) {
                model.addAttribute("matkul", matakuliahKurikulum);
                if (matakuliahKurikulum.getStatus() == null){
                    matakuliahKurikulum.setStatus(StatusRecord.NONAKTIF);
                }
                MatkulDto matkulDto = new MatkulDto();
                matkulDto.setIdMat(matakuliahKurikulum.getMatakuliah().getId());
                matkulDto.setNamaMatakuliah(matakuliahKurikulum.getMatakuliah().getNamaMatakuliah());
                matkulDto.setKodeMatakuliah(matakuliahKurikulum.getMatakuliah().getKodeMatakuliah());
                matkulDto.setNamaMatakuliahEnglish(matakuliahKurikulum.getMatakuliah().getNamaMatakuliahEnglish());
                matkulDto.setSingkatan(matakuliahKurikulum.getMatakuliah().getSingkatan());
                matkulDto.setResponsi(matakuliahKurikulum.getResponsi());
                matkulDto.setNourut(matakuliahKurikulum.getNomorUrut());
                matkulDto.setWajib(matakuliahKurikulum.getWajib());
                matkulDto.setKurikulum(matakuliahKurikulum.getKurikulum());
                matkulDto.setMatakuliahKurikulumSemester(matakuliahKurikulum.getMatakuliahKurikulumSemester());
                matkulDto.setSemester(matakuliahKurikulum.getSemester());
                matkulDto.setSyaratTugas(matakuliahKurikulum.getSyaratTugasAkhir());
                matkulDto.setSks(matakuliahKurikulum.getJumlahSks());
                matkulDto.setIpkMinimal(matakuliahKurikulum.getIpkMinimal());
                matkulDto.setKonsentrasi(matakuliahKurikulum.getKonsentrasi());
                matkulDto.setNamaFile(matakuliahKurikulum.getSilabus());
                model.addAttribute("matkul",matkulDto);

            }
        }


    }

    @PostMapping("/matakuliah/form")
    public String prosesMatkul(@ModelAttribute @Valid MatkulDto matkulDto, MultipartFile silabus, BindingResult errors) throws Exception {
        System.out.println(matkulDto.getIdMat());
        String namaFile = silabus.getName();
        String jenisFile = silabus.getContentType();
        String namaAsli = silabus.getOriginalFilename();
        Long ukuran = silabus.getSize();

        String extension = "";

        int i = namaAsli.lastIndexOf('.');
        int p = Math.max(namaAsli.lastIndexOf('/'), namaAsli.lastIndexOf('\\'));

        if (i > p) {
            extension = namaAsli.substring(i + 1);
        }

        String idFile = UUID.randomUUID().toString();
        String lokasiUpload = uploadFolder;
        new File(lokasiUpload).mkdirs();
        File tujuan = new File(lokasiUpload + File.separator + idFile + "." + extension);
        silabus.transferTo(tujuan);


        if (matkulDto.getIdMat() == null || matkulDto.getIdMat().isEmpty()){
            Matakuliah matakuliah = new Matakuliah();
            matakuliah.setIdProdi(matkulDto.getProdi());
            matakuliah.setKodeMatakuliah(matkulDto.getKodeMatakuliah());
            matakuliah.setNamaMatakuliah(matkulDto.getNamaMatakuliah());
            matakuliah.setNamaMatakuliahEnglish(matkulDto.getNamaMatakuliahEnglish());
            matakuliah.setSingkatan(matkulDto.getSingkatan());
            mataKuliahDao.save(matakuliah);

            MatakuliahKurikulum matakuliahKurikulum = new MatakuliahKurikulum();
            BeanUtils.copyProperties(matkulDto,matakuliahKurikulum);
            if (matkulDto.getResponsi() == null){
                matakuliahKurikulum.setResponsi("N");
            }

            if (matkulDto.getWajib() == null){
                matakuliahKurikulum.setResponsi("N");
            }
            matakuliahKurikulum.setMatakuliah(matakuliah);
            matakuliahKurikulum.setJumlahSks(matkulDto.getSks());
            matakuliahKurikulum.setNomorUrut(matkulDto.getNourut());
            matakuliahKurikulum.setSyaratTugasAkhir(matkulDto.getSyaratTugas());
            matakuliahKurikulum.setSilabus(idFile + "." + extension);
            matakuliahKurikulumDao.save(matakuliahKurikulum);
            System.out.println(matakuliahKurikulum);
        }

        if (matkulDto.getIdMat() != null || !matkulDto.getIdMat().isEmpty()){
            Matakuliah matakuliah = mataKuliahDao.findById(matkulDto.getIdMat()).get();

            if (matakuliah.getNamaMatakuliah().equals(matkulDto.getNamaMatakuliah())){
                MatakuliahKurikulum matakuliahKurikulum = new MatakuliahKurikulum();
                BeanUtils.copyProperties(matkulDto,matakuliahKurikulum);
                matakuliahKurikulum.setMatakuliah(matakuliah);
                if (matkulDto.getResponsi() == null){
                    matakuliahKurikulum.setResponsi("N");
                }
                if (matkulDto.getWajib() == null){
                    matakuliahKurikulum.setResponsi("N");
                }
                matakuliahKurikulum.setJumlahSks(matkulDto.getSks());
                matakuliahKurikulum.setNomorUrut(matkulDto.getNourut());
                matakuliahKurikulum.setSyaratTugasAkhir(matkulDto.getSyaratTugas());
                matakuliahKurikulum.setSilabus(idFile + "." + extension);
                matakuliahKurikulumDao.save(matakuliahKurikulum);
                System.out.println(matakuliahKurikulum);
            }else {
                Matakuliah m = new Matakuliah();
                m.setIdProdi(matkulDto.getProdi());
                m.setKodeMatakuliah(matkulDto.getKodeMatakuliah());
                m.setNamaMatakuliahEnglish(matkulDto.getNamaMatakuliahEnglish());
                m.setNamaMatakuliah(matkulDto.getNamaMatakuliah());
                m.setSingkatan(matkulDto.getSingkatan());
                mataKuliahDao.save(m);

                MatakuliahKurikulum matakuliahKurikulum = new MatakuliahKurikulum();
                BeanUtils.copyProperties(matkulDto,matakuliahKurikulum);
                matakuliahKurikulum.setMatakuliah(m);
                if (matkulDto.getResponsi() == null){
                    matakuliahKurikulum.setResponsi("N");
                }

                if (matkulDto.getWajib() == null){
                    matakuliahKurikulum.setResponsi("N");
                }
                matakuliahKurikulum.setJumlahSks(matkulDto.getSks());
                matakuliahKurikulum.setNomorUrut(matkulDto.getNourut());
                matakuliahKurikulum.setSyaratTugasAkhir(matkulDto.getSyaratTugas());
                matakuliahKurikulum.setSilabus(idFile + "." + extension);
                matakuliahKurikulumDao.save(matakuliahKurikulum);
                System.out.println(matakuliahKurikulum);
            }


        }



        return "redirect:list?prodi="+matkulDto.getProdi().getId()+"&kurikulum="+matkulDto.getKurikulum().getId();
    }
}
