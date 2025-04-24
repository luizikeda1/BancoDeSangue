package com.testecitel.demo.repository;

import com.testecitel.demo.model.BancoSangue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BancoSangueDAO extends CrudRepository<BancoSangue, Long> {

    @Query(nativeQuery = true, value = "" +
            " select  " +
            "\tid,  " +
            "\tnome,  " +
            "\tcelular,  " +
            "\temail,  " +
            "\tcpf  " +
            "from  " +
            "\tbanco_sangue  " +
            "where  " +
            "\t(:nome = ''  " +
            "\t\tor nome like CONCAT('%', :nome, '%'))  " +
            "\tand (:cpf = ''  " +
            "\t\tor cpf like CONCAT('%', :cpf, '%'))  " +
            "\tand (:sexo = 'X'  " +
            "\t\tor sexo = :sexo)  " +
            "\tand (:tipoSanguineo = 'X'  " +
            "\t\tor tipo_sanguineo = :tipoSanguineo)  " +
            "\tand (:estado = 'X'  " +
            "\t\tor estado = :estado) ")
    Page<Object[]> filtrarDoador(
            @Param("nome") String nome,
            @Param("cpf") String cpf,
            @Param("sexo") String sexo,
            @Param("tipoSanguineo") String tipoSanguineo,
            @Param("estado") String estado,
            Pageable pageable);

    boolean existsByCpf(String cpf);
}
