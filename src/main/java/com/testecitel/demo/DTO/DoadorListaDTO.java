package com.testecitel.demo.DTO;

import java.text.ParseException;

public class DoadorListaDTO {

    private String nome;
    private String telefone;
    private String email;
    private String cpf;
    private String id;


    public DoadorListaDTO() {}

    public DoadorListaDTO(Object [] o) throws ParseException {
        this.nome = o[1].toString();
        this.telefone = o[2].toString();
        this.email = o[3].toString();
        this.cpf = o[4].toString();
        this.id = o[0].toString();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
