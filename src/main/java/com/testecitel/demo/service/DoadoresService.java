package com.testecitel.demo.service;

import com.testecitel.demo.DTO.DoadorFilter;
import com.testecitel.demo.DTO.DoadorListaDTO;
import com.testecitel.demo.repository.BancoSangueDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoadoresService {

    @Autowired
    BancoSangueDAO bancoSangueDAO;

    private String[] cols = {"nome", "celular", "email", "cpf", "id"};

    public Map<String, Object> filtrarDoador(DoadorFilter filter, HttpServletRequest request) throws ParseException {

        String drawParam = request.getParameter("draw");
        int draw = drawParam != null ? Integer.parseInt(drawParam) : 0;
        Pageable pageable = pagination(request, cols);

        Page<Object[]> objects;
        objects = bancoSangueDAO.filtrarDoador(filter.getNome(), filter.getCpf(), filter.getSexo(), filter.getTipoSanguineo(), filter.getEstado(), pageable);

        return clienteDTOToJson(objects, draw);


    }
    @NotNull
    private static Map<String, Object> clienteDTOToJson(Page<Object[]> objects, int draw) {

        List<DoadorListaDTO> doadorListaDTOS = objects.stream()
                .map(obj -> {
                    try {
                        return new DoadorListaDTO(obj);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        Map<String, Object> json = new HashMap<>();
        json.put("draw", draw);
        json.put("recordsTotal", objects.getTotalElements());
        json.put("recordsFiltered", objects.getTotalElements());
        json.put("data", doadorListaDTOS);

        return json;
    }

    private Pageable pagination(HttpServletRequest request, String[] cols) {
        int start = Integer.parseInt(request.getParameter("start"));
        int length = Integer.parseInt(request.getParameter("length"));

        int current = currentPage(start, length);

        String column = columnName(request, cols);
        Sort.Direction direction = orderBy(request);
        return PageRequest.of(current, length, direction, column);
    }

    private Sort.Direction orderBy(HttpServletRequest request) {
        String order = request.getParameter("order[0][dir]");
        Sort.Direction sort = Sort.Direction.ASC;
        if (order.equalsIgnoreCase("desc")) {
            sort = Sort.Direction.DESC;
        }
        return sort;
    }

    private String columnName(HttpServletRequest request, String[] cols) {
        int iCol = Integer.parseInt(request.getParameter("order[0][column]"));
        return cols[iCol];
    }

    private int currentPage(int start, int length) {
        return start / length;
    }
}
