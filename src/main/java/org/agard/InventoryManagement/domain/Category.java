package org.agard.InventoryManagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(exclude = "products")
@ToString(exclude = "products")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    @NotBlank(message = "Category Name cannot be blank")
    @Size(min = 3, max = 20, message = "Category Name must be between 3 and 20 characters")
    private String name;

    @Builder.Default
    @OneToMany(mappedBy = "category")
    private Set<Product> products = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @Version
    private Integer version;

    public void addProduct(Product product){
        //System.out.println("adding product");
        this.products.add(product);
    }

    @PreRemove
    private void beforeDeletion(){
        for(Product p : products){
            p.dereferenceCategory();
        }
    }
}
