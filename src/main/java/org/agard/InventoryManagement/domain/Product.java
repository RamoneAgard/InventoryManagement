package org.agard.InventoryManagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
@NoArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Product name required")
    private String name;

    @ManyToOne
    private Category category;

    @Positive(message = "Enter a valid price value")
    @NotNull(message = "Enter a product price")
    private BigDecimal price;

    @Positive(message = "Enter a valid cost value")
    @NotNull(message = "Enter a product cost")
    private BigDecimal cost;

    @PositiveOrZero(message = "Enter a non-negative in-stock value")
    @NotNull(message = "Enter an in-stock value")
    private Integer stock;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;

    @Version
    private Integer version;


    public Product(Long id, String name, Category category, BigDecimal price, BigDecimal cost, Integer stock, LocalDateTime createdDate, LocalDateTime lastModifiedDate, Integer version) {
        this.id = id;
        this.name = name;
        this.setCategory(category);
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.version = version;
    }

    public void setCategory(Category category){
        this.category = category;
        category.addProduct(this);
    }

    public void dereferenceCategory(){
        this.category = null;
    }
}
