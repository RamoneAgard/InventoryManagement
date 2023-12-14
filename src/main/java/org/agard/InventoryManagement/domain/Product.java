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
@AllArgsConstructor
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    @NotNull
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
    @NotNull
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

    @Builder.Default
    private boolean deleted = false;

}
