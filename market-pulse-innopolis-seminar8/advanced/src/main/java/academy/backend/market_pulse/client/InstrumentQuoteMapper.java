package academy.backend.market_pulse.client;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;

/**
 * Общий маппинг {@link InstrumentQuoteDto} → {@link Quote} для обеих реализаций
 * {@link TInvestApiClient} (см. пакет {@code client.impl}) — парсинг JSON у них разный, а
 * восстановление домена из DTO одно и то же.
 */
public final class InstrumentQuoteMapper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private InstrumentQuoteMapper() {
    }

    /** Разбор тела ответа {@code instruments-service} (JSON-массив) в котировки. */
    public static List<Quote> fromJson(String body) {
        try {
            List<InstrumentQuoteDto> dtos = OBJECT_MAPPER.readValue(body, OBJECT_MAPPER.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
            return dtos.stream().map(InstrumentQuoteMapper::toQuote).toList();
        } catch (IOException e) {
            throw new TInvestApiException("Не удалось разобрать ответ instruments-service", e);
        }
    }

    public static Quote toQuote(InstrumentQuoteDto dto) {
        Instrument instrument = InstrumentFactories.create(
                dto.getType(), dto.getTicker(), dto.getName(), Currency.valueOf(dto.getCurrency()));
        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
    }

    /**
     * Обратное преобразование — используется persistence-слоем (см. «Требования к файловому
     * хранилищу.md», семинар 7) для сохранения {@link Quote} в том же DTO-формате, в котором их
     * отдаёт instruments-service.
     */
    public static InstrumentQuoteDto toDto(Quote quote) {
        Instrument instrument = quote.getInstrument();
        return new InstrumentQuoteDto(
                instrument.getTicker(),
                instrument.getName(),
                instrument.getType(),
                instrument.getCurrency().name(),
                quote.getPrice(),
                quote.getChangePercent());
    }
}
