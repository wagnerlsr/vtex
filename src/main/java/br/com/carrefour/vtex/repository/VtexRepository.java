package br.com.carrefour.vtex.repository;

import br.com.carrefour.vtex.model.Vtex;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VtexRepository extends MongoRepository<Vtex, String> {
}
