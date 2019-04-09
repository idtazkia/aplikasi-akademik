package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.dao.KurikulumDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MataKuliahDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.akademik.aplikasiakademik.dao.ProdiDao;
import id.ac.tazkia.akademik.aplikasiakademik.dto.MatkulDto;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class MataKuliahController {

    @Autowired
    private KurikulumDao kurikulumDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private MataKuliahDao mataKuliahDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @GetMapping("/api/matakuliah")
    @ResponseBody
    public Page<Matakuliah> cariData(@RequestParam(required = false) String search, Pageable page){
        if(!StringUtils.hasText(search)) {
            return mataKuliahDao.findAll(page);
        }
        return mataKuliahDao.findByNamaMatakuliahContainingIgnoreCase(search, page);

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
    public void  formMataKuliah(@RequestParam(required = false) String prodi,
                                @RequestParam(required = false)String id,Model model){
        model.addAttribute("prodi",prodiDao.findById(prodi).get());
        model.addAttribute("listKurikulum",kurikulumDao.findByStatusNotIn(StatusRecord.HAPUS));

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
                model.addAttribute("matkul",matkulDto);

            }
        }


    }

    @PostMapping("/matakuliah/form")
    public String prosesMatkul(@ModelAttribute @Valid MatkulDto matkulDto, BindingResult errors){
        System.out.println(matkulDto.getIdMat());
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
            matakuliahKurikulum.setMatakuliah(matakuliah);
            matakuliahKurikulum.setJumlahSks(matkulDto.getSks());
            matakuliahKurikulum.setNomorUrut(matkulDto.getNourut());
            matakuliahKurikulum.setSyaratTugasAkhir(matkulDto.getSyaratTugas());
            matakuliahKurikulumDao.save(matakuliahKurikulum);
            System.out.println(matakuliahKurikulum);
        }

        if (matkulDto.getIdMat() != null || !matkulDto.getIdMat().isEmpty()){
            Matakuliah matakuliah = mataKuliahDao.findById(matkulDto.getIdMat()).get();

            MatakuliahKurikulum matakuliahKurikulum = new MatakuliahKurikulum();
            BeanUtils.copyProperties(matkulDto,matakuliahKurikulum);
            matakuliahKurikulum.setMatakuliah(matakuliah);
            matakuliahKurikulum.setJumlahSks(matkulDto.getSks());
            matakuliahKurikulum.setNomorUrut(matkulDto.getNourut());
            matakuliahKurikulum.setSyaratTugasAkhir(matkulDto.getSyaratTugas());
            matakuliahKurikulumDao.save(matakuliahKurikulum);
            System.out.println(matakuliahKurikulum);

        }



        return "redirect:list";
    }
}
