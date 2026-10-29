package academy.backend.market_pulse.repository.store.impl;

import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import academy.backend.market_pulse.factory.InstrumentFactories;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.repository.store.QuoteFileStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import academy.backend.market_pulse.dto.InstrumentQuoteDto;
/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (JSON, CSV, построчный текст); главное,
 * чтобы {@code load(save(quotes))} возвращал те же котировки. Проверьте себя тестом
 * {@code QuoteFileStoreContractTest} (замените в нём эталонную реализацию на свою).
 */
public class QuoteFileStoreSkeleton implements QuoteFileStore {

    private final ObjectMapper objectMapper;

    public QuoteFileStoreSkeleton() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void save(Path path, List<Quote> quotes) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            List<InstrumentQuoteDto> dtos = quotes.stream()
                    .map(q -> new InstrumentQuoteDto(
                            q.getInstrument().getTicker(),
                            q.getInstrument().getName(),
                            q.getInstrument().getType(),
                            q.getInstrument().getCurrency().name(),
                            q.getPrice(),
                            q.getChangePercent()
                    ))
                    .toList();

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), dtos);
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось сохранить котировки в файл: " + path, e);
        }
    }

    @Override
    public List<Quote> load(Path path) {
        if (!Files.exists(path)) {
            return Collections.emptyList();
        }
        try {
            List<InstrumentQuoteDto> dtos = objectMapper.readValue(path.toFile(), new TypeReference<List<InstrumentQuoteDto>>() {});

            return dtos.stream()
                    .map(dto -> {
                        Instrument instrument = InstrumentFactories.create(
                                dto.getType(),
                                dto.getTicker(),
                                dto.getName(),
                                Currency.valueOf(dto.getCurrency()) // Превращаем String обратно в Enum Currency
                        );
                        return new Quote(instrument, dto.getPrice(), dto.getChangePercent());
                    })
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать котировки из файла: " + path, e);
        }
    }
}