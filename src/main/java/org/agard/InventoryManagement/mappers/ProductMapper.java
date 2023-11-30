package org.agard.InventoryManagement.mappers;

import org.agard.InventoryManagement.ViewModels.ItemProduct;
import org.agard.InventoryManagement.ViewModels.ProductForm;
import org.agard.InventoryManagement.domain.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "volumeId", source = "volume.id")
    ProductForm productToProductForm(Product product);

    @Mapping(target = "volumeDescription", source = "volume.description")
    ItemProduct productToItemProduct(Product product);
}
