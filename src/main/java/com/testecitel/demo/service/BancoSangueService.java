package com.testecitel.demo.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testecitel.demo.model.BancoSangue;
import com.testecitel.demo.repository.BancoSangueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class BancoSangueService {

    @Autowired
    private BancoSangueDAO bancoSangueDAO;

    public void importarDadosJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data.json");

        if (inputStream == null) {
            throw new FileNotFoundException("Arquivo data.json não encontrado");
        }

        TypeReference<List<BancoSangue>> typeReference = new TypeReference<List<BancoSangue>>() {};
        List<BancoSangue> lista = mapper.readValue(inputStream, typeReference);
        bancoSangueDAO.saveAll(lista);
    }
}