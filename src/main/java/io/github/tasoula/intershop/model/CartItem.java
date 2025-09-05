package io.github.tasoula.intershop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.sql.Timestamp;
import java.util.UUID;

@Table(name = "t_cart_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItem {
    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    @Column("product_id")
    private UUID productId;

    private int quantity;

    private Timestamp createdAt;

    //todo: добавить version

    public CartItem(UUID userId, UUID productId) {
        this.userId = userId;
        this.productId = productId;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
