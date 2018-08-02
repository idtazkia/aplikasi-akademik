package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.JenjangDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JenjangController {

    @Autowired
    JenjangDao jenjangDao;

    @GetMapping("/jenjang/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page){
        return new ModelMap()
                .addAttribute("list",jenjangDao.findByStatus(StatusConstants.Aktif,page));
    }

    @GetMapping("/jenjang/form")
    public void  formFakultas(){
    }

    @PostMapping("/delete/jenjang")
    public String hapusJenjang(@RequestParam Jenjang jenjang,Authentication currentUser){

        LOGGER.debug("Authentication class : {}", currentUser.getClass().getName());

        if (currentUser == null) {
            LOGGER.warn("Current user is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        LOGGER.debug("User ID : {}", u.getId());
        if (u == null) {
            LOGGER.warn("Username {} not found in database ", username);
        }

        jenjang.setUserEdit(u);
        jenjang.setTglEdit(LocalDateTime.now());
        jenjang.setStatus(StatusConstants.Nonaktif);
        jenjangDao.save(jenjang);

        return "redirect:/jenjang/list";
    }
}
