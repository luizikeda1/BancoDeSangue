package com.testecitel.demo.repository;

import com.testecitel.demo.model.BancoSangue;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BancoSangueDAO extends CrudRepository<BancoSangue, Long> {
}
