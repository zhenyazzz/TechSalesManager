package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
