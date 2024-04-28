package br.com.carrefour.vtex.repository;

import br.com.carrefour.vtex.model.SkuContext;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkuContextRepository extends MongoRepository<SkuContext, String> {
    public Optional<SkuContext> findBySkuId(Long skuId);
}
