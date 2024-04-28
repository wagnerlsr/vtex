package br.com.carrefour.vtex.repository;

import br.com.carrefour.vtex.model.SkuContext;
import br.com.carrefour.vtex.model.SkuId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkuIdRepository extends MongoRepository<SkuId, String> {

    @Query("{'skuId': {$gt: ?0, $lte: ?1}}")
    public List<SkuId> findBySkuIdRange(Long skuIdStart, Long skuIdEnd);

    public Optional<SkuId> findBySkuId(Long skuId);

    public List<SkuId> findByAdjustedEquals(boolean adjusted);

}
