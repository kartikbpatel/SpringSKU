package com.app.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.app.model.Sku;

public interface SkuRepository extends CrudRepository<Sku, Long> {
	List<Sku> findAll();
}
