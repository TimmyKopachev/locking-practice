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
public class Account {

    private static final String BLACK_BG_COLOR = "\u001B[40m";
    private static final String WHITE_COLOR = "\u001B[37m";
    public static final String COLOR_RESET = "\u001B[0m";

    @Id
    @Column(name = "uuid")
    private String uuid;
    @Column(scale = 15, precision = 30)
    private BigDecimal balance;
    @Column
    private String owner;

    @Version
    private Long version;


    public void withdraw(BigDecimal money) {
        log.info("{}{} Client:<{}> withdraws money:<{}> from balance:<{}>.{}",
                BLACK_BG_COLOR, WHITE_COLOR,
                owner, money, balance,
                COLOR_RESET);
        this.balance = balance.subtract(money, new MathContext(4, RoundingMode.FLOOR));
    }

    public void transfer(BigDecimal money) {
        log.info("{}{} Client:<{}> received money:<{}>. Initial balance:<{}>.{}",
                BLACK_BG_COLOR, WHITE_COLOR,
                owner, money, balance,
                COLOR_RESET);
        this.balance = balance.add(money, new MathContext(4, RoundingMode.FLOOR));
    }

}
