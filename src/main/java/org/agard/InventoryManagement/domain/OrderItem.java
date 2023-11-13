package org.agard.InventoryManagement.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class OrderItem {

    @Id
    private Long id;

    //private Product product;

    private Integer quantity;
}
