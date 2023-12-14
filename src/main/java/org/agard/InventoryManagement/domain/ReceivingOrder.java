package org.agard.InventoryManagement.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReceivingOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String supplier;

    @NotNull
    @OneToMany(orphanRemoval = true)
    @Builder.Default
    private Set<OrderItem> items = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    private LocalDateTime lastModifiedDate;

    @Version
    private Integer version;

    @Transient
    public BigDecimal getTotalCost(){
        BigDecimal sum = BigDecimal.ZERO;
        for(OrderItem item : items){
            sum = sum.add( item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())) );
        }
        return sum;
    }

    public void setItems(Set<OrderItem> itemsToSet){
        if(itemsToSet != null){
            this.items.clear();
            this.items.addAll(itemsToSet);
        }
    }

}
