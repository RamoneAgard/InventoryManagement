package org.agard.InventoryManagement.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class OrderItem {

    @Id
    private Long id;

    @ManyToOne
    private Product product;

    private Integer requestedQuantity;

    private Integer receivedQuantity;
}
