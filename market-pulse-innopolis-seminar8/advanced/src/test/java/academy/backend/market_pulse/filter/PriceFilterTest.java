package academy.backend.market_pulse.filter;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Тест фильтра по цене: пишется вживую человеком. Эталон — {@code PriceFilterReferenceTest}.
 */
@Disabled("Пишется вживую")
class PriceFilterTest {

    // NOTICE: техника — классы эквивалентности + граничные значения. Порог один (5.0), три класса
    // для акции: ниже порога, ровно на пороге, выше порога. Результат трёх операторов (GE/LE/EQ)
    // различается ТОЛЬКО на границе и рядом с ней — именно её проверяем в первую очередь.
    // NOTICE: отдельный класс эквивалентности — не акция (облигация, ETF): фильтр не проходят никогда,
    // независимо от оператора и порога.
    // NOTICE: одна таблица вместо девяти копий теста — @ParameterizedTest + @CsvSource
    // (оператор, дивидендная доходность, ожидаемый результат); проверка — assertThat(...).isEqualTo(...)
    // из AssertJ.
    // TODO: @ParameterizedTest @CsvSource: оператор, доходность акции, ожидаемое matches(...)
    // TODO: не-акция (Bond/Etf) -> false для любого оператора
    @Test
    void todo() {
    }
}
