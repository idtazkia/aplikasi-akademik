package id.ac.tazkia.smilemahasiswa.controller;

import id.ac.tazkia.smilemahasiswa.dao.KuesionerDao;
import id.ac.tazkia.smilemahasiswa.dao.KuesionerPertanyaanDao;
import id.ac.tazkia.smilemahasiswa.dao.KuesionerPilihanGandaDao;
import id.ac.tazkia.smilemahasiswa.dto.kusioner.KuesionerDto;
import id.ac.tazkia.smilemahasiswa.entity.*;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class KuesionerController {

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private KuesionerDao kuesionerDao;

    @Autowired
    private KuesionerPertanyaanDao kuesionerPertanyaanDao;

    @Autowired
    private KuesionerPilihanGandaDao kuesionerPilihanGandaDao;

    @GetMapping("/kuesioner")
    public String listKuesioner(Model model, @PageableDefault Pageable pageable){
        model.addAttribute("list", kuesionerDao.findByStatus(StatusRecord.AKTIF, pageable));
        return "kuesioner/list";
    }

    @GetMapping("/kuesioner/form")
    public String formKuesioner(Model model, @RequestParam Kuesioner kuesioner){
        model.addAttribute("kuesioner", kuesioner);
        model.addAttribute("listPertanyaan", kuesionerPertanyaanDao.findByStatusAndKuesionerOrderByUrutanAsc(StatusRecord.AKTIF, kuesioner));
        model.addAttribute("urutan", kuesionerPertanyaanDao.countByStatusAndKuesioner(StatusRecord.AKTIF, kuesioner));
        return "kuesioner/form";
    }

    @GetMapping("/kuesioner/deskripsi")
    public String formDeskripsi(Model model, @RequestParam(required = false) Kuesioner id){
        if (id == null){
            model.addAttribute("kuesioner", new Kuesioner());
        }else {
            model.addAttribute("kuesioner", id);
        }
        return "kuesioner/deskripsi";
    }

    @PostMapping("/kuesioner/deskripsi/save")
    public String saveDeskripsi(@Valid Kuesioner kuesioner, Authentication authentication,
                                RedirectAttributes attributes){
        try{
            User user = currentUserService.currentUser(authentication);
            kuesioner.setCreatedBy(user.getUsername());
            kuesioner.setCreatedDate(LocalDateTime.now());
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Disimpan");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @GetMapping("/kuesioner/deskripsi/delete")
    public String deleteDeskripsi(@RequestParam Kuesioner kuesioner, RedirectAttributes attributes){
        try{
            kuesioner.setStatus(StatusRecord.HAPUS);
            kuesionerDao.save(kuesioner);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner";
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner";
        }
    }

    @PostMapping("/kuesioner/pertanyaan/save")
    public String savePertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                int row = 1;
                for (String data : pilihanGanda){
                    KuesionerPilihanGanda kuesionerPilihanGanda = new KuesionerPilihanGanda();
                    kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                    kuesionerPilihanGanda.setUrutan(row++);
                    kuesionerPilihanGanda.setOpsi(data);
                    kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                }
            }else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil disimpan");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @PostMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(@Valid KuesionerDto kuesionerDto, RedirectAttributes attributes){
        try{
            KuesionerPertanyaan kuesionerPertanyaan = new KuesionerPertanyaan();
            kuesionerPertanyaan.setId(kuesionerDto.getId());
            kuesionerPertanyaan.setKuesioner(kuesionerDto.getKuesioner());
            kuesionerPertanyaan.setPertanyaan(kuesionerDto.getPertanyaan());
            kuesionerPertanyaan.setJenisPertanyaan(kuesionerDto.getJenisPertanyaan());
            kuesionerPertanyaan.setWajibIsi(kuesionerDto.getWajibIsi());
            kuesionerPertanyaan.setCreatedDate(LocalDateTime.now());
            kuesionerPertanyaan.setUrutan(kuesionerDto.getUrutan());
            if (kuesionerDto.getJenisPertanyaan().equals("pilihanGanda")){
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
                ArrayList<String> pilihanGanda = kuesionerDto.getPilihanGanda();
                ArrayList<String> idPilihanGanda = kuesionerDto.getIdPilihanGanda();
                int row = 1;

                for (int i = 0; i < pilihanGanda.size(); i++) {
                    String dataPilihanGanda = pilihanGanda.get(i);
                    String dataId = i < idPilihanGanda.size() ? idPilihanGanda.get(i) : null;

                    if (dataId == null || dataId.isEmpty()) {
                        KuesionerPilihanGanda newKuesionerPilihanGanda = new KuesionerPilihanGanda();
                        newKuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        newKuesionerPilihanGanda.setUrutan(row++);
                        newKuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(newKuesionerPilihanGanda);
                    } else {
                        KuesionerPilihanGanda kuesionerPilihanGanda = kuesionerPilihanGandaDao.findById(dataId).orElse(new KuesionerPilihanGanda());
                        kuesionerPilihanGanda.setKuesionerPertanyaan(kuesionerPertanyaan);
                        kuesionerPilihanGanda.setUrutan(row++);
                        kuesionerPilihanGanda.setOpsi(dataPilihanGanda);
                        kuesionerPilihanGandaDao.save(kuesionerPilihanGanda);
                    }
                }
            } else {
                kuesionerPertanyaan.setSkalaLinearAwal(kuesionerDto.getSkalaLinearAwal());
                kuesionerPertanyaan.setSkalaLinearAkhir(kuesionerDto.getSkalaLinearAkhir());
                kuesionerPertanyaan.setOpsiSkalaLinearAwal(kuesionerDto.getOpsiSkalaLinearAwal());
                kuesionerPertanyaan.setOpsiSkalaLinearAkhir(kuesionerDto.getOpsiSkalaLinearAkhir());
                kuesionerPertanyaanDao.save(kuesionerPertanyaan);
            }
            attributes.addFlashAttribute("success", "Berhasil diedit");
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + kuesionerDto.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/delete")
    public String deletePertanyaan(@RequestParam KuesionerPertanyaan id, RedirectAttributes attributes){
        try{
            id.setStatus(StatusRecord.HAPUS);
            kuesionerPertanyaanDao.save(id);
            attributes.addFlashAttribute("success", "Berhasil Dihapus");
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }catch (Exception e){
            attributes.addFlashAttribute("error", "Error : " + e.getMessage());
            return "redirect:/kuesioner/form?kuesioner=" + id.getKuesioner().getId();
        }
    }

    @GetMapping("/kuesioner/pertanyaan/edit")
    public String editPertanyaan(Model model, @RequestParam KuesionerPertanyaan id){
        KuesionerDto kuesionerDto = new KuesionerDto();
        BeanUtils.copyProperties(id, kuesionerDto);
        model.addAttribute("kuesionerPertanyaan", kuesionerDto);
        return "kuesioner/edit";
    }

    @ResponseBody
    @GetMapping("/list-pilihanganda")
    public List<KuesionerPilihanGanda> listPilihanGanda(@RequestParam KuesionerPertanyaan id){
        List<KuesionerPilihanGanda> kuesionerPilihanGandas = kuesionerPilihanGandaDao
                .findByStatusAndKuesionerPertanyaanOrderByUrutanAsc(StatusRecord.AKTIF, id);
        return kuesionerPilihanGandas;
    }

    @ResponseBody
    @GetMapping("/delete/pilihanGanda")
    public KuesionerPilihanGanda deletePilihanGanda(@RequestParam KuesionerPilihanGanda id){
        id.setStatus(StatusRecord.HAPUS);
        kuesionerPilihanGandaDao.save(id);

        return id;
    }
}
