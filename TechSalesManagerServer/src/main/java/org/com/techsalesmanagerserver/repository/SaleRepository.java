package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}
