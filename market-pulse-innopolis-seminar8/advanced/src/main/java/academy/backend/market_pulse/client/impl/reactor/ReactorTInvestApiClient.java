package academy.backend.market_pulse.client.impl.reactor;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.client.InstrumentQuoteMapper;
import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;
import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.exception.TInvestApiException;
import academy.backend.market_pulse.model.Quote;

/**
 * Реакторная реализация {@link TInvestApiClient} — та же задача, что у
 * {@code HttpClientTInvestApiClient}, но без потока, блокирующегося на ожидании ответа (см. «План
 * семинара.md», семинар 7, этап 7). {@link #fetchQuotes()} удобства ради синхронный (чтобы
 * реализовывать общий интерфейс и сравниваться с блокирующим на равных), но под капотом —
 * {@link #fetchQuotesAsync()}: вся выгода реактора проявляется именно там, когда вызывается
 * конкурентно много раз на одном {@link Reactor} (см. {@code ClientThroughputDemo}).
 */
public class ReactorTInvestApiClient implements TInvestApiClient, AutoCloseable {

    private final Reactor reactor;
    private final InetSocketAddress address;
    private final String mockScenario;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ReactorTInvestApiClient(TInvestApiProperties properties) {
        this.address = new InetSocketAddress(properties.getHost(), properties.getPort());
        this.mockScenario = properties.getMockScenario();
        this.reactor = new Reactor();
        this.reactor.start();
    }

    @Override
    public List<Quote> fetchQuotes() {
        try {
            return fetchQuotesAsync().join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new TInvestApiException("Не удалось выполнить реакторный запрос к instruments-service", cause);
        }
    }

    /** Асинхронный путь — несколько вызовов подряд делят один и тот же поток событийного цикла. */
    public CompletableFuture<List<Quote>> fetchQuotesAsync() {
        byte[] request = RawHttpProtocol.buildGetRequest(
                address.getHostName(), address.getPort(), "/v1/instruments", mockScenario);
        return reactor.submit(address, request).thenApply(this::parseQuotes);
    }

    private List<Quote> parseQuotes(byte[] rawResponse) {
        RawHttpProtocol.ParsedResponse response = RawHttpProtocol.parse(rawResponse);
        if (response.statusCode() != 200) {
            throw new TInvestApiException("instruments-service вернул неожиданный статус: " + response.statusCode());
        }
        try {
            List<InstrumentQuoteDto> dtos = objectMapper.readValue(response.body(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
            return dtos.stream().map(InstrumentQuoteMapper::toQuote).toList();
        } catch (Exception e) {
            throw new TInvestApiException("Не удалось разобрать ответ instruments-service", e);
        }
    }

    @Override
    public void close() {
        reactor.close();
    }
}
