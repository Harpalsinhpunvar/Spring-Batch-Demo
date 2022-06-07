package com.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.batch.model.Product;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	//reader
	@Bean
	public FlatFileItemReader<Product> reader(){
		FlatFileItemReader<Product> reader = new FlatFileItemReader<>();
		reader.setResource(new ClassPathResource("rec.csv"));
		reader.setLineMapper(getLineMapper());
		reader.setLinesToSkip(1);
		return reader;
		
	}

	private LineMapper<Product> getLineMapper() {
		// TODO Auto-generated method stub
		
		DefaultLineMapper<Product> lineMapper = 
				new DefaultLineMapper<>();
		
		DelimitedLineTokenizer lt = new DelimitedLineTokenizer();
		lt.setNames(new String[] {"ProductIt", "Region", "City", "Category", "Product", "Quantity", "UnitPrice", "TotalPrice"});
		lt.setIncludedFields(new int[] {0, 1, 2, 3, 4, 5, 6, 7});
		
		BeanWrapperFieldSetMapper<Product> fs = new BeanWrapperFieldSetMapper<Product>();
		fs.setTargetType(Product.class);
		
		lineMapper.setLineTokenizer(lt);
		lineMapper.setFieldSetMapper(fs);
		return lineMapper;
	}
	
	//processor
	@Bean
	public ProductItemProcessor processor() {
		return new ProductItemProcessor();
		
	}
	
	//writer
	@Bean
	public JdbcBatchItemWriter<Product> writer(){
		
		JdbcBatchItemWriter<Product> writer = 
				new JdbcBatchItemWriter<Product>();
		
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<Product>());
		writer.setSql("insert into product(productId, region, city, category, product, quantity, unitPrice, totalPrice) values (:productId, :region, :city, :category, :product, :quantity, :unitPrice, :totalPrice)");
		writer.setDataSource(this.dataSource);
		return writer;
		
	}
	
	//Job
	@Bean
	public Job importUserJob() {
		
		return this.jobBuilderFactory.get("PRODUCT-IMPORT-JOB")
				.incrementer(new RunIdIncrementer())
				.flow(step1())
				.end()
				.build();
		
	}

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1")
		.<Product, Product>chunk(10)
		.reader(reader())
		.processor(processor())
		.writer(writer())
		.build();
	}

}
