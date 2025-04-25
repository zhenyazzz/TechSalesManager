package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
