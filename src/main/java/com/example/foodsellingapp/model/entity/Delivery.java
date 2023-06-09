package com.example.foodsellingapp.model.entity;

import com.example.foodsellingapp.model.eenum.StatusDelivery;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Table
@Entity
@Getter
@Setter
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long shipperId;
    private Long orderId;
    @Column(name = "status_delivery")
    private StatusDelivery statusDelivery;
}
