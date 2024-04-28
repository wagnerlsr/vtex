package br.com.carrefour.vtex.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Document("vtex")
public class Vtex {

    @Id
    private String id;

    private Map<String, Object> dados;

}
