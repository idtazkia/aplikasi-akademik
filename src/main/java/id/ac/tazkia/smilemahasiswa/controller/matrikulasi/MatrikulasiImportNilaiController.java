package id.ac.tazkia.smilemahasiswa.controller.matrikulasi;


import id.ac.tazkia.smilemahasiswa.dao.MahasiswaDao;
import id.ac.tazkia.smilemahasiswa.dao.ProdiDao;
import id.ac.tazkia.smilemahasiswa.dao.TahunAkademikDao;
import id.ac.tazkia.smilemahasiswa.entity.TahunAkademik;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MatrikulasiImportNilaiController {

    @Autowired
    private TahunAkademikDao tahunAkademikDao;

    @Autowired
    private ProdiDao prodiDao;

    @Autowired
    private MahasiswaDao mahasiswaDao;

    @GetMapping("/matrikulasi/importNilai")
    public String matrikulasiImporNilai(Model model,
                                        @RequestParam(required = false) String angkatan){

        model.addAttribute("listAngkatanMahasiswa", mahasiswaDao.listAngkatanMahasiswa());

        return "matrikulasi/import_nilai/list";
    }

}
