package nnt_data.bankaccount_microservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCardEntity {
    @Id
    private String id;
    private String cardNumber;
    private Date expirationDate;
    private String primaryAccountId;
    private List<String> associatedAccountIds = new ArrayList<>();
}
