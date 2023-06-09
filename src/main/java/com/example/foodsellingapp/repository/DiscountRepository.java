package com.example.foodsellingapp.repository;

import com.example.foodsellingapp.model.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends JpaRepository<Discount,Integer> {
    Discount findByCodeDiscount(String codeDiscount);
}
