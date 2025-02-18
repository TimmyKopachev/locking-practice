package org.dzmitry.kapachou.locking.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Data
@Entity
@Table
@Slf4j
@NamedEntityGraph(
        name = "graph.client",
        attributeNodes = @NamedAttributeNode(value = "client")
)
public class Account {

    @Id
    @Column(name = "uuid")
    private String uuid;
    @Column(scale = 15, precision = 30)
    private BigDecimal balance;
    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Client client;

    public void withdraw(BigDecimal money) {
        log.debug("Client:<{}> withdraws money:<{}> from balance:<{}>.", getClient().getName(), money, balance);
        this.balance = balance.subtract(money, new MathContext(4, RoundingMode.FLOOR));
    }

    public void transfer(BigDecimal money) {
        log.debug("Client:<{}> received money:<{}>. Initial balance:<{}>.", getClient().getName(), money, balance);
        this.balance = balance.add(money, new MathContext(4, RoundingMode.FLOOR));
    }

}
