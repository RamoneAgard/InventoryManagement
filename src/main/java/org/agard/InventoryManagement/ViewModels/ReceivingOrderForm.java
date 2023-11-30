package org.agard.InventoryManagement.ViewModels;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.agard.InventoryManagement.domain.OrderItem;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReceivingOrderForm {

    private Long id;

    @NotBlank
    private String supplier;

    @NotNull
    @Builder.Default
    private List<OrderItemForm> items = new ArrayList<>();

    private LocalDateTime createdDate;

    private LocalDateTime lastModifiedDate;
}
