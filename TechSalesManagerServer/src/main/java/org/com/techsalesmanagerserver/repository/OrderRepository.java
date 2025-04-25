package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
