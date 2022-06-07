package com.batch.config;

import org.springframework.batch.item.ItemProcessor;

import com.batch.model.Product;

public class ProductItemProcessor implements ItemProcessor<Product, Product> {

	@Override
	public Product process(Product product) throws Exception {
		
		return product;
	}
	
	

}
