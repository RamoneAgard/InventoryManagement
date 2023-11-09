package org.agard.InventoryManagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "products")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Volume {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Volume Description cannot be blank")
    @Size(min = 3, max = 30)
    private String description;

    @Positive
    @NotNull(message = "Value cannot be null")
    private Integer valueCode;

    @Builder.Default
    @OneToMany(mappedBy = "volume")
    private Set<Product> products = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Version
    private Integer version;

    public void addProduct(Product product){
        this.products.add(product);
    }

    private void beforeDeletion(){
        for(Product p : products){
            p.dereferenceVolume();
        }
    }
}