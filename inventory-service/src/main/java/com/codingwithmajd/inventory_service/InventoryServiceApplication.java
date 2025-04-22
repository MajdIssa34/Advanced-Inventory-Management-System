package com.codingwithmajd.inventory_service;

import com.codingwithmajd.inventory_service.Repo.InventoryRepo;
import com.codingwithmajd.inventory_service.model.Inventory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepo inventoryRepo){
		return args -> {
			Inventory inventory = new Inventory();
			inventory.setSkuCode("iphone_13");
			inventory.setQuantity(100);

			Inventory inventory1 = new Inventory();
			inventory.setSkuCode("iphone_13_black");
			inventory.setQuantity(0);

			inventoryRepo.save(inventory);
			inventoryRepo.save(inventory1);
		};
	}
}
