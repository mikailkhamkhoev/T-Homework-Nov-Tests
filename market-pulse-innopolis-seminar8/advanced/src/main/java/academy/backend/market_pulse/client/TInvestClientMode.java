package academy.backend.market_pulse.client;

import academy.backend.market_pulse.client.impl.HttpClientTInvestApiClient;
import academy.backend.market_pulse.client.impl.HttpUrlConnectionTInvestApiClient;
import academy.backend.market_pulse.client.impl.reactor.ReactorTInvestApiClient;
import academy.backend.market_pulse.config.TInvestApiProperties;

/**
 * Простой переключатель реализации клиента (см. «План семинара.md», семинар 7) — не
 * полноценный feature-flagging фреймворк, просто `enum` и фабричный метод: вызывающему коду
 * неважно, какая реализация выбрана, все — {@link TInvestApiClient}.
 */
public enum TInvestClientMode {
    URL_CONNECTION,
    REACTOR,
    HTTP_CLIENT;

    public static TInvestApiClient createClient(TInvestClientMode mode, TInvestApiProperties properties) {
        return switch (mode) {
            case URL_CONNECTION -> new HttpUrlConnectionTInvestApiClient(properties);
            case REACTOR -> new ReactorTInvestApiClient(properties);
            case HTTP_CLIENT -> new HttpClientTInvestApiClient(properties);
        };
    }
}
