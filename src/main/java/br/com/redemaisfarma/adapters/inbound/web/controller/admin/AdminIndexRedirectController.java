/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  jakarta.servlet.http.HttpServletRequest
 *  org.springframework.context.annotation.Profile
 *  org.springframework.http.HttpStatus
 *  org.springframework.http.HttpStatusCode
 *  org.springframework.http.ResponseEntity
 *  org.springframework.http.ResponseEntity$BodyBuilder
 *  org.springframework.stereotype.Controller
 *  org.springframework.web.bind.annotation.PostMapping
 *  org.springframework.web.bind.annotation.RequestMapping
 *  org.springframework.web.bind.annotation.RequestMethod
 *  org.springframework.web.servlet.view.RedirectView
 */
package br.com.redemaisfarma.adapters.inbound.web.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

@Profile(value={"!test"})
@Controller
@RequestMapping(value={"/admin"})
public class AdminIndexRedirectController {
    private static final String TARGET = "/admin/dashboard";

    @RequestMapping(path={"", "/"}, method={RequestMethod.GET, RequestMethod.HEAD})
    public String redirectGet() {
        return "redirect:/admin/dashboard";
    }

    @PostMapping(value={"", "/"})
    public RedirectView redirectPost() {
        RedirectView rv = new RedirectView(TARGET);
        rv.setStatusCode((HttpStatusCode)HttpStatus.SEE_OTHER);
        return rv;
    }

    @RequestMapping(path={"", "/"}, method={RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.TRACE})
    public ResponseEntity<Void> redirectOthers(HttpServletRequest req) {
        return ((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)((ResponseEntity.BodyBuilder)ResponseEntity.status((HttpStatusCode)HttpStatus.TEMPORARY_REDIRECT).header("Location", new String[]{req.getContextPath() + TARGET})).header("Cache-Control", new String[]{"no-store, no-cache, must-revalidate, max-age=0"})).header("Pragma", new String[]{"no-cache"})).header("Referrer-Policy", new String[]{"no-referrer"})).build();
    }
}

