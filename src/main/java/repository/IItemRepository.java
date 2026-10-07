package repository;

import entity.Item;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IItemRepository extends MongoRepository<Item, String> {
}
