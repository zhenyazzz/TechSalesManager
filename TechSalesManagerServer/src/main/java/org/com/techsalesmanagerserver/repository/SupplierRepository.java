package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    List<Supplier> findByName(String name);
    List<Supplier> findByProductsId(Long productId);
} 