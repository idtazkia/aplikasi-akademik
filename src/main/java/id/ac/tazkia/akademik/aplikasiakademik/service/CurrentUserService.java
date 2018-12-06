package id.ac.tazkia.akademik.aplikasiakademik.service;

import id.ac.tazkia.akademik.aplikasiakademik.dao.UserDao;
import id.ac.tazkia.akademik.aplikasiakademik.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrentUserService.class);

    @Autowired
    private UserDao userDao;

    public User currentUser(Authentication currentUser){
        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) currentUser;

        String username = (String) token.getPrincipal().getAttributes().get("email");
        User u = userDao.findByUsername(username);
        return u;
    }
}
