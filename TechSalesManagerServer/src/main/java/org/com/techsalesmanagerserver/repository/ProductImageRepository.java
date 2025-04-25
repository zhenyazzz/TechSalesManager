package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
}
