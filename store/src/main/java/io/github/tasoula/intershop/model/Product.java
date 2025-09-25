package io.github.tasoula.intershop.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;


@Table(name = "t_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Product {
    @Id
    private UUID id;

    private String title;

    private String description;

    @Column("image_path")
    private String imgPath;

    private BigDecimal price;

    @Column("stock_quantity")
    private int stockQuantity;

}
