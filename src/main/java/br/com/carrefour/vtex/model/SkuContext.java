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
@Document("sku_context")
public class SkuContext {
    @Id
    private String id;

    private Long skuId;
    private Long productId;
    private boolean isActive;
    private String name;
    private String creationDate;
    private boolean isKit;
    private boolean refIdOk;
    private boolean eansOk;
    private Date insertDate;
    private Date updateDate;

    private List<Ean> eans = new ArrayList<>();
    private List<RefId> refIds = new ArrayList<>();


    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class Ean {
        private Date queryDate;
        private Date correctionDate;

        private List<String> eans = new ArrayList<>();
    }


    @Getter
    @Setter
    @ToString
    @RequiredArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class RefId {
        private Date queryDate;
        private Date correctionDate;
        private String refId;
    }
}
