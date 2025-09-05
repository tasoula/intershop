package io.github.tasoula.intershop.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Table(name = "t_orders")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Order {
    @Id
    private UUID id;

    @Column("user_id")
    private User user;

    @CreatedDate //  Аннотация для автоматического заполнения поля при создании записи
    // Убедитесь, что в вашей базе данных колонка created_at определена как TIMESTAMP или аналогичный тип, и что она не имеет значения по умолчанию, если вы используете @CreatedDate
    @Column("created_at")
    private Timestamp createdAt;

    @Column("total_amount")
    private BigDecimal totalAmount;

    //todo: добавить version
}
