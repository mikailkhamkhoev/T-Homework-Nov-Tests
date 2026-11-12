package academy.backend.market_pulse.config;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import academy.backend.market_pulse.client.TInvestApiClient;
import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.impl.PriceHistoryStoreReference;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.model.Watchlist;
import academy.backend.market_pulse.repository.FileBackedQuoteRepository;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import academy.backend.market_pulse.repository.QuoteFileWatcher;
import academy.backend.market_pulse.repository.store.QuoteFileStore;
import academy.backend.market_pulse.repository.store.WatchlistStore;
import academy.backend.market_pulse.repository.store.impl.QuoteFileStoreReference;
import academy.backend.market_pulse.repository.store.impl.WatchlistStoreReference;
import academy.backend.market_pulse.service.QuoteService;
import academy.backend.market_pulse.service.QuoteSource;

/**
 * Композиционный корень приложения: единственное место, где создаются все зависимости, и каждая —
 * ровно один раз (синглтон на время работы процесса). {@link academy.backend.market_pulse.cli.CommandLineFactory}
 * берёт готовые объекты отсюда, а не создаёт их сам.
 */
@Getter
public class AppConfig {

    private final InstrumentRepository instrumentRepository;
    private final QuoteSource quoteSource;
    private final QuoteService quoteService;

    private final WatchlistStore watchlistStore;
    private final Path watchlistPath;
    private final Watchlist watchlist;

    private final TInvestApiClient apiClient;

    private final QuoteFileStore quoteFileStore;
    private final FileBackedQuoteRepository fileBackedQuoteRepository;

    private final PriceHistoryStore priceHistoryStore;
    private final Path historyDirectory;

    private final Map<String, List<BigDecimal>> priceHistory;

    public AppConfig() {
        this.instrumentRepository = new InMemoryInstrumentRepository();
        this.quoteSource = new QuoteSource(sampleQuotes(instrumentRepository));
        this.quoteService = new QuoteService(quoteSource);

        // Watchlist переживает перезапуск CLI — читается при старте, дописывается на диск сразу
        // при изменении (см. WatchCommand).
        this.watchlistStore = new WatchlistStoreReference();
        this.watchlistPath = Path.of("data", "watchlist.json");
        this.watchlist = new Watchlist(watchlistStore.load(watchlistPath));

        // Семинар 7: клиент к instruments-service (моковый сервер в Docker, см.
        // docker/mockserver) — настройки отдельным классом, клиент создаётся один раз здесь и
        // дальше инжектится через конструктор.
        // Переменная окружения MARKET_PULSE_MOCK_SCENARIO (error | timeout | ok) включает сбой на
        // стороне мокового сервера: MARKET_PULSE_MOCK_SCENARIO=error java ... update
        TInvestApiProperties properties = TInvestApiProperties.localMock()
                .withMockScenario(System.getenv("MARKET_PULSE_MOCK_SCENARIO"));
        this.apiClient = new HttpClientTInvestApiClient(properties);

        // Самостоятельная работа (см. «Требования к файловому хранилищу.md»): котировки
        // отслеживаемых инструментов — на диске, а не только в памяти процесса.
        // QuoteFileStoreReference — эталон для сверки в начале семинара 8.
        this.quoteFileStore = new QuoteFileStoreReference();
        this.fileBackedQuoteRepository =
                new FileBackedQuoteRepository(quoteFileStore, Path.of("data", "quotes.json"));
        new QuoteFileWatcher(fileBackedQuoteRepository).start();

        // История цен — для будущей статистики: новая точка (новый файл) на каждое update.
        this.priceHistoryStore = new PriceHistoryStoreReference();
        this.historyDirectory = Path.of("data", "history");

        this.priceHistory = samplePriceHistory();
    }

    /**
     * Заглушка каталога и котировок: репозиторий уже содержит базовый набор (засеян в конструкторе
     * {@code InMemoryInstrumentRepository}), здесь добавляются остальные инструменты и по каждому
     * строится котировка. Возвращает котировки для {@link QuoteSource}.
     */
    private static Map<String, Quote> sampleQuotes(InstrumentRepository repository) {
        Instrument sber = repository.findByTicker("SBER").orElseThrow();
        Instrument ofz = repository.findByTicker("OFZ26233").orElseThrow();
        Instrument fxus = repository.findByTicker("FXUS").orElseThrow();

        Instrument gazp = InstrumentFactories.create("STOCK", "GAZP", "Газпром", Currency.RUB);
        Instrument lkoh = InstrumentFactories.create("STOCK", "LKOH", "Лукойл", Currency.RUB);
        Instrument aapl = InstrumentFactories.create("STOCK", "AAPL", "Apple", Currency.USD);
        repository.add(gazp);
        repository.add(lkoh);
        repository.add(aapl);

        Map<String, Quote> quotes = new LinkedHashMap<>();
        quotes.put("SBER", new Quote(sber, new BigDecimal("250.00"), new BigDecimal("1.20")));
        quotes.put("GAZP", new Quote(gazp, new BigDecimal("120.50"), new BigDecimal("-0.80")));
        quotes.put("LKOH", new Quote(lkoh, new BigDecimal("7100.00"), new BigDecimal("2.30")));
        quotes.put("AAPL", new Quote(aapl, new BigDecimal("225.30"), new BigDecimal("0.50")));
        quotes.put("OFZ26233", new Quote(ofz, new BigDecimal("780.00"), new BigDecimal("-0.15")));
        quotes.put("FXUS", new Quote(fxus, new BigDecimal("6.85"), new BigDecimal("0.90")));
        return quotes;
    }

    /**
     * Заглушка истории цен по тикерам — для команды {@code history} (скользящее среднее через
     * Gatherer API). Отдельная {@code Map}, не часть {@code QuoteSource}: истории нет у реального
     * источника.
     */
    private static Map<String, List<BigDecimal>> samplePriceHistory() {
        Map<String, List<BigDecimal>> history = new LinkedHashMap<>();
        history.put("SBER", List.of(
                new BigDecimal("244.00"), new BigDecimal("246.50"), new BigDecimal("245.00"),
                new BigDecimal("248.00"), new BigDecimal("250.00"), new BigDecimal("249.50"),
                new BigDecimal("251.20")));
        history.put("LKOH", List.of(
                new BigDecimal("6950.00"), new BigDecimal("7020.00"), new BigDecimal("6990.00"),
                new BigDecimal("7080.00"), new BigDecimal("7100.00"), new BigDecimal("7150.00")));
        return history;
    }
}
