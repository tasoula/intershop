package io.github.tasoula.intershop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "t_order_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OrderItem {
    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("product_id")
    private UUID productId;

    private Integer quantity;

    @Column("price_at_time_of_order")
    private BigDecimal priceAtTimeOfOrder;
}
