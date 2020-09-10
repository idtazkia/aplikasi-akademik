package id.ac.tazkia.smilemahasiswa.controller.setting;


import id.ac.tazkia.smilemahasiswa.dao.MataKuliahSetaraDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahDao;
import id.ac.tazkia.smilemahasiswa.dao.MatakuliahKurikulumDao;
import id.ac.tazkia.smilemahasiswa.entity.Matakuliah;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahKurikulum;
import id.ac.tazkia.smilemahasiswa.entity.MatakuliahSetara;
import id.ac.tazkia.smilemahasiswa.service.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MatakuliahSetaraController {

    @Autowired
    private MatakuliahDao matakuliahDao;

    @Autowired
    private MatakuliahKurikulumDao matakuliahKurikulumDao;

    @Autowired
    private MataKuliahSetaraDao mataKuliahSetaraDao;

    @Autowired
    private CurrentUserService currentUserService;

    @GetMapping("/setting/matakuliah/setara")
    public String listMatakuliahSetara(Model model,
                                       @RequestParam(required = false) MatakuliahKurikulum matakuliahKurikulum){

        model.addAttribute("matakuliahKurikulum", matakuliahKurikulumDao.findById(matakuliahKurikulum.getId()).get());
        model.addAttribute("listMatakuliahSetara", mataKuliahSetaraDao.listMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId()));

        return "setting/matakuliah_setara/list";

    }

    @GetMapping("/setting/matakuliah/setara/tambah")
    public String tambahMatakuliahSetara(Model model,
                                         @PageableDefault(size = 10) Pageable page,
                                         @RequestParam(required = false) MatakuliahKurikulum matakuliahKurikulum){

        model.addAttribute("matakuliahKurikulum", matakuliahKurikulum);
        model.addAttribute("listMatakuliah" , matakuliahDao.pilihMatakuliahSetara(matakuliahKurikulum.getMatakuliah().getId(), page));

        return "setting/matakuliah_setara/form";

    }

    @PostMapping("/setting/matakuliah/setara/pilih")
    public String pilihMatakuliahSetara(Model model,
                                        @RequestParam(required = false) Matakuliah matakuliah,
                                        @RequestParam(required = false) MatakuliahKurikulum matakuliahKurikulum){



        return "redirect:../tambah?matakuliahKurikulum="+ matakuliahKurikulum.getId();
    }

}
