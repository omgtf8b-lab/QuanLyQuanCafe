package com.lotus.lani_coffe.repository;

import com.lotus.lani_coffe.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByAvailable(Boolean available);
    
    @Query("SELECT DISTINCT p.brand FROM Product p WHERE p.brand IS NOT NULL")
    List<String> findDistinctBrands();

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIdAndPriceBetween(Long categoryId, Double minPrice, Double maxPrice);
}
