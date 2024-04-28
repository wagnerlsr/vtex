package br.com.carrefour.vtex.listener;

import br.com.carrefour.vtex.model.SkuContext;
import br.com.carrefour.vtex.model.SkuId;
import br.com.carrefour.vtex.repository.SkuIdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Date;


@Slf4j
@Component
@RequiredArgsConstructor
public class VTexListener {
    private final SkuIdRepository skuIdRepository;

    @JmsListener(destination = "carrefour_vtex_queue", containerFactory = "defaultFactory")
    public void receiveMessage(SkuId skuId) {
        var sku = skuIdRepository.findBySkuId(skuId.getSkuId()).orElse(skuId);

        if (sku.getSkuId() != null) {
            sku.setUpdateDate(new Date());
            sku.setCorrectionDate(null);
            sku.setAdjusted(false);
        }

        skuIdRepository.save(sku);
    }
}
