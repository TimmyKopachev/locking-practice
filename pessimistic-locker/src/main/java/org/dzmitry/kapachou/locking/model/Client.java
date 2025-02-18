package org.dzmitry.kapachou.locking.model;

import lombok.Data;
import jakarta.persistence.*;


@Data
@Entity
@Table
public class Client {

    @Id
    private String uuid;

    private String name;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "account_uuid")
    private Account account;

    @Override
    public String toString() {
        return String.format("Client:<%s>", this.name);
    }
}
