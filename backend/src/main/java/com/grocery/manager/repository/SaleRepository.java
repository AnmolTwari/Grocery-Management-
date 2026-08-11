package com.grocery.manager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grocery.manager.entity.Sale;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}