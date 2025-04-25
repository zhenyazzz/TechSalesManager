package org.com.techsalesmanagerserver.repository;

import org.com.techsalesmanagerserver.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CategoryRepository extends JpaRepository<Category,Long> {
}
