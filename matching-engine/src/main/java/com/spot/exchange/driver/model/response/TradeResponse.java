package com.spot.exchange.driver.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeResponse {

    private long id;
    private String symbol;
    private long buy;
    private long sell;
    private String quantity;
    private String price;
    @JsonProperty("isBuyerMaker")
    private boolean buyerMaker;
    private long timestamp;
}
