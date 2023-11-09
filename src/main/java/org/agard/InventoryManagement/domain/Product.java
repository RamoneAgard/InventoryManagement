package org.agard.InventoryManagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.validator.constraints.Length;

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

    @NotBlank(message = "Non-blank, 12 digit universal product code required ")
    @Size(min = 12, max = 12)
    @Column(unique = true)
    private String upc;

    @NotBlank(message = "Non-blank, 6 character item code required")
    @Size(min = 6, max= 7)
    @Column(unique = true)
    private String itemCode;

    @Size(min = 2, max = 40)
    @NotBlank(message = "Non-blank product name required")
    private String name;

    @ManyToOne
    private Category category;

    @Positive(message = "Valid price value required")
    @NotNull(message = "Enter a product price")
    private BigDecimal price;

    @Positive(message = "Valid cost value required")
    @NotNull(message = "Enter a product cost")
    private BigDecimal cost;

    @Positive(message = "Valid unit size required")
    @NotNull(message = "Enter a unit size")
    private Integer unitSize;

    @ManyToOne
    private Volume volume;

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


    public Product(Long id, String upc, String itemCode, String name, Category category, BigDecimal price, BigDecimal cost, Integer unitSize, Volume volume, Integer stock, LocalDateTime createdDate, LocalDateTime lastModifiedDate, Integer version) {
        this.id = id;
        this.upc = upc;
        this.itemCode = itemCode;
        this.name = name;
        this.setCategory(category);
        this.price = price;
        this.cost = cost;
        this.unitSize = unitSize;
        this.setVolume(volume);
        this.stock = stock;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.version = version;
    }

    public void setCategory(Category category){
        this.category = category;
        category.addProduct(this);
    }

    public void setVolume(Volume volume){
        this.volume = volume;
        volume.addProduct(this);
    }

    public void dereferenceCategory(){
        this.category = null;
    }

    public void dereferenceVolume(){
        this.volume = null;
    }
}
