package br.com.carrefour.vtex.repository;

import br.com.carrefour.vtex.model.Sku;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuRepository extends MongoRepository<Sku, String> {

    public Optional<Sku> findBySkuId(Long skuId);
    public List<Sku> findAllByAdjusted(boolean adjusted);

}
