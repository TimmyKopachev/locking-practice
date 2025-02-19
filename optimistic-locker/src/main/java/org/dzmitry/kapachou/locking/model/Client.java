package org.dzmitry.kapachou.locking.model;

import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table
public class Client {

    @Id
    private String uuid;

    private String name;

    @OneToOne(fetch = FetchType.EAGER, targetEntity = Account.class)
    @JoinColumn(name = "account_uuid")
    private Account account;

    @Version
    private Long version;

    @Override
    public String toString() {
        return String.format("Client:<%s>", this.name);
    }
}
