package id.ac.tazkia.akademik.aplikasiakademik.controller;

import id.ac.tazkia.akademik.aplikasiakademik.constants.StatusConstants;
import id.ac.tazkia.akademik.aplikasiakademik.dao.*;
import id.ac.tazkia.akademik.aplikasiakademik.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.time.LocalDateTime;

@Controller
public class ProgramController {


    private static final Logger LOGGER = LoggerFactory.getLogger(ProgramController.class);


    @Autowired
    ProgramDao programDao;

    @Autowired
    UserDao userDao;

    @GetMapping("/program/list")
    public ModelMap list(@PageableDefault(direction = Sort.Direction.ASC) Pageable page) {
        return new ModelMap()
                .addAttribute("list", programDao.findByStatusNotIn(StatusRecord.HAPUS, page));
    }

    @GetMapping("/program/form")
    public String  formFakultas(Model model,Authentication currentUser, @RequestParam(required = false)String id){

        model.addAttribute("program", new Program());

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

        if (id != null && !id.isEmpty()) {
            Program program = programDao.findById(id).get();
            if (program != null) {
                program.setUserEdit(u);
                program.setTglEdit(LocalDateTime.now());
                model.addAttribute("program", program);
            }
        }
        return "/program/form";
    }


    @PostMapping(value = "/program/form")
    public String uploadBukti(@Valid Program program,
                              BindingResult error,@RequestParam(required = false) Program id,
                              Authentication currentUser){

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

        if (id != null){
            program.setTglEdit(LocalDateTime.now());
            program.setUserEdit(u);
            program.setTglInsert(id.getTglInsert());
            program.setUserInsert(id.getUserInsert());
        }

        if (id == null) {
            program.setUserInsert(u);
            program.setTglInsert(LocalDateTime.now());
        }

        programDao.save(program);


        return "redirect:/program/list";

    }

    @PostMapping(value = "/program/delete")
    public String deleteProgram(@RequestParam Program program,Authentication currentUser) {

        LOGGER.debug("Authentication class : ()", currentUser.getClass().getName());

        if(currentUser == null){
            LOGGER.warn("Current User is null");
        }

        String username = ((UserDetails) currentUser.getPrincipal()).getUsername();
        User u = userDao.findByUsername(username);
        LOGGER.debug("User ID : {}", u.getId());
        if(u == null){
            LOGGER.warn("Username {} not found in database", username);
        }

        program.setStatus(StatusRecord.NONAKTIF);
        program.setUserEdit(u);
        program.setTglEdit(LocalDateTime.now());
        programDao.save(program);


        return "redirect:/program/list";
    }
}