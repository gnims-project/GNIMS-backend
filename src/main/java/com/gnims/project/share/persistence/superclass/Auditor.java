package com.gnims.project.share.persistence.superclass;

import com.gnims.project.security.service.UserDetailsImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Slf4j
public class Auditor implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        SecurityContext context = SecurityContextHolder.getContext();

        Object principal = null;
        try {
            principal = context.getAuthentication().getPrincipal();
        } catch (NullPointerException e) {
//            log.info("[알 수 없는 에러 발생 : Auditor - NullPointerException]");
            return Optional.empty();
        }

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            Long id = userDetails.getUser().getId();
            return Optional.of(id);
        }

        return Optional.empty();
    }
}
