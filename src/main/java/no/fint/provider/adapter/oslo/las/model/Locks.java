package no.fint.provider.adapter.oslo.las.model;

import lombok.ToString;

import java.util.Map;

@ToString
public class Locks {
    public Map<String, Lock> doors;
}
