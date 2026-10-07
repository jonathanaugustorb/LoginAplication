package entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class Item {
    @Id
    private String id;
    private String name;
    private String description;
    private String price;
}
