package no.fint.provider.adapter.oslo.las.model;

import lombok.ToString;

import java.math.BigDecimal;

@ToString
public class Lock {

    public BigDecimal last_seen;
    public int status;
}
