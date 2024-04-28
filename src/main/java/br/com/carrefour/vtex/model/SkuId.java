package br.com.carrefour.vtex.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Document("sku_id")
public class SkuId {

    @Id
    private String id;

    private Long skuId;
    private Date insertDate;
    private Date updateDate;
    private Date correctionDate;
    private boolean adjusted;

    public SkuId(Long skuId, Date date) {
        this.skuId = skuId;
        this.insertDate = date;
        this.updateDate = null;
        this.correctionDate = null;
        this.adjusted = false;
    }
}
