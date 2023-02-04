package com.gnims.project.util;

import com.gnims.project.security.user.UserDetailsImpl;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

public class Auditor implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        SecurityContext context = SecurityContextHolder.getContext();
        Object principal = context.getAuthentication().getPrincipal();

        if (principal instanceof UserDetailsImpl) {
            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            Long id = userDetails.getUser().getId();
            return Optional.of(id);
        }

        return Optional.empty();
    }
}
