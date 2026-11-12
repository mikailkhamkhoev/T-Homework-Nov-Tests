package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import academy.backend.market_pulse.client.InstrumentQuoteMapper;
import academy.backend.market_pulse.dto.InstrumentQuoteDto;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;

/**
 * Эталон для разбора в начале семинара 8 при необходимости (см. «Требования к файловому
 * хранилищу.md», семинар 7) — не единственно верный ответ. JSON через {@link InstrumentQuoteDto} —
 * тот же формат, в котором отвечает моковый instruments-service, переиспользован для файла на
 * диске: не нужно придумывать второй формат под то же самое.
 */
public class QuoteFileStoreReference implements QuoteFileStore {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void save(Path path, List<Quote> quotes) {
        List<InstrumentQuoteDto> dtos = quotes.stream().map(InstrumentQuoteMapper::toDto).toList();
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dtos);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        if (!Files.exists(path)) {
            return List.of();
        }
        try {
            List<InstrumentQuoteDto> dtos = objectMapper.readValue(path.toFile(), objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, InstrumentQuoteDto.class));
            return dtos.stream().map(InstrumentQuoteMapper::toQuote).toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из " + path, e);
        }
    }
}
