package br.com.carrefour.vtex.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Document("skus")
public class Sku {

    @Id
    private String id;

    private Long skuId;
    private String skuEan;
    private Long idProduto;
    private String codigoReferenciaProduto;
    private Date createdEans;
    private Date deletedEans;

    private boolean adjusted = false;

    private List<String> eans = new ArrayList<>();
    private List<String> cEans = new ArrayList<>();

    private List<String> fixEans = new ArrayList<>();
    private List<String> fixCEans = new ArrayList<>();

}
