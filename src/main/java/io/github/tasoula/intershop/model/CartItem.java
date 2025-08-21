package io.github.tasoula.intershop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "t_cart_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, columnDefinition = "integer default 0")
    private int quantity;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    public CartItem(User user, Product product) {
        this.user = user;
        this.product = product;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
