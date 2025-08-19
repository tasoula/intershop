package io.github.tasoula.intershop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductCatalogItemDto {
    private UUID id;
    private String title;
    private String description;
    private String imgPath;
    private BigDecimal price;
    private int stockQuantity;
    private int cartQuantity;
    public List<String> getTextParts() {
        return Arrays.stream(description.split("\\r?\\n")).collect(Collectors.toList());
    }
}
