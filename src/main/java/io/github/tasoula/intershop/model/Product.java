package io.github.tasoula.intershop.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "t_products")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_path", unique = true)
    private String imgPath;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    // todo - убрать
    public int getCount(){
        return 0;
    }

    // todo сделать dto и перенести это туда
    private final static int PREVIEW_LENGTH = 200;

    // если на витрине не отображать описание, то этот метод не нужен
    public String getDescriptionPreview() {
        int delimiterIndex = description.indexOf("\r\n");
        if (delimiterIndex != -1)
            return description.substring(0, Math.min(PREVIEW_LENGTH, delimiterIndex)) + "...";

        if (description.length() > PREVIEW_LENGTH)
            return description.substring(0, PREVIEW_LENGTH) + "...";

        return description;
    }

    public List<String> getTextParts() {
        return Arrays.stream(description.split("\\r?\\n")).collect(Collectors.toList());
    }
}
