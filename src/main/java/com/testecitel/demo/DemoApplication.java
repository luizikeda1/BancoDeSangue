package com.testecitel.demo;

import com.testecitel.demo.service.BancoSangueService;
import com.testecitel.demo.repository.BancoSangueDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    @Autowired
    private BancoSangueService bancoSangueService;

    @Autowired
    private BancoSangueDAO bancoSangueDAO;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        if (bancoSangueDAO.count() == 0) {
            bancoSangueService.importarDadosJson();
            System.out.println("Dados importados com sucesso.");
        } else {
            System.out.println("Tabela BancoSangue já contém dados. Importação não realizada.");
        }
    }
}