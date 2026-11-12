package academy.backend.market_pulse.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

/**
 * Запись ответа мокового {@code instruments-service}: инструмент и его текущая котировка в одном
 * объекте — упрощение ради учебного мока (настоящий T-Invest API разносит каталог инструментов и
 * рыночные данные по разным сервисам). См. «План семинара.md», семинар 7, этап 2.
 */
@Getter
public class InstrumentQuoteDto {

    private final String ticker;
    private final String name;
    private final String type;
    private final String currency;
    private final BigDecimal price;
    private final BigDecimal changePercent;

    @JsonCreator
    public InstrumentQuoteDto(
            @JsonProperty("ticker") String ticker,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("currency") String currency,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("changePercent") BigDecimal changePercent) {
        this.ticker = ticker;
        this.name = name;
        this.type = type;
        this.currency = currency;
        this.price = price;
        this.changePercent = changePercent;
    }
}
