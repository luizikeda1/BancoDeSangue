package com.testecitel.demo.controller;

import com.testecitel.demo.DTO.DoadorFilter;
import com.testecitel.demo.service.DoadoresService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.text.ParseException;
import java.util.Map;

@Controller
@RequestMapping(path = "/doadores")
public class DoadoresController {

    @Autowired
    DoadoresService doadoresService;

    @RequestMapping
    public ModelAndView getDoadores() {
        ModelAndView mv = new ModelAndView("/banco/list_doadores");
        return mv;
    }

    @GetMapping("/pesquisar")
    public ResponseEntity<Map<String, Object>> filtrarDoador(DoadorFilter filter, HttpServletRequest request) throws ParseException {
        Map<String, Object>  doador = doadoresService.filtrarDoador(filter, request);
        return ResponseEntity.ok(doador);
    }
}
