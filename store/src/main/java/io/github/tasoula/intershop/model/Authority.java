package io.github.tasoula.intershop.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("t_authorities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Authority {
    @Id
    private UUID id;
    private String authority;
}