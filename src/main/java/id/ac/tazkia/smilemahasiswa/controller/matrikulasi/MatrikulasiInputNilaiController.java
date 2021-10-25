package id.ac.tazkia.smilemahasiswa.controller.matrikulasi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MatrikulasiInputNilaiController {

    @GetMapping("/matrikulasi/inputNilai")
    public String matrikulasiInputNilai(){


        return "matrikulasi/input_nilai/list";
    }

}
