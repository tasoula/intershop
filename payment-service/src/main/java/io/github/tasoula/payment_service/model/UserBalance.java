package io.github.tasoula.payment_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Table(name = "t_balance")
public class UserBalance {

    @Id
    private UUID id;

    @Column("user_id")
    private UUID userId;

    private BigDecimal balance;

    public UserBalance() {}

    public UserBalance(UUID id, UUID userId, BigDecimal balance) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }
    public void setUserId(UUID userId) {
        this.userId = userId;
    }
    public BigDecimal getBalance() {
        return balance;
    }
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }


}

