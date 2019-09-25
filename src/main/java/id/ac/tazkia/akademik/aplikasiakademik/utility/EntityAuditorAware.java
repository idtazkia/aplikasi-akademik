package id.ac.tazkia.akademik.aplikasiakademik.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class EntityAuditorAware implements AuditorAware<String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityAuditorAware.class);

    @Override
    public Optional<String> getCurrentAuditor() {
        OAuth2AuthenticationToken auth = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        auth.getPrincipal().getAttributes().get("email");
        LOGGER.debug("Authentication Object : {}", auth);
        
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(OAuth2AuthenticationToken.class::cast)
                .map(OAuth2AuthenticationToken::getPrincipal)
                .map(OAuth2User::getAttributes)
                .map(x -> x.get("email"))
                .map(String.class::cast);
    }
}
