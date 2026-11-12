package academy.backend.market_pulse.exception;

/**
 * Сбой при обращении к T-Invest API (сетевая ошибка, неожиданный статус ответа, битый JSON) —
 * оборачивает исходную причину, не теряя её (см. «План семинара.md», семинар 7, этап 2).
 */
public class TInvestApiException extends RuntimeException {

    public TInvestApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public TInvestApiException(String message) {
        super(message);
    }
}
