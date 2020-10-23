package top.siyile.facusapi;

import lombok.Getter;

public class CounterResponse {
    @Getter private final int number;
    @Getter private final String results;

    public CounterResponse(int number, String results) {
        this.number = number;
        this.results = results;
    }
}