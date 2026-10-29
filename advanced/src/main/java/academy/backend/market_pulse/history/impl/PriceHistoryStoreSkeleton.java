package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (бинарный, текстовый и т.д.); главное,
 * чтобы каждый {@link #append} создавал НОВЫЙ файл (не переписывал предыдущий) и
 * {@link #load(Path, String)} возвращал все сохранённые точки по тикеру в порядке возрастания
 * времени. Проверьте себя тестом {@code PriceHistoryStoreContractTest} (замените в нём эталонную
 * реализацию на свою).
 */
public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    private final ObjectMapper objectMapper;

    public PriceHistoryStoreSkeleton() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Path append(Path directory, PricePoint point) {
        try {
            Files.createDirectories(directory);

            String safeTimestamp = point.timestamp().toString().replace(':', '-');
            String fileName = String.format("%s_%s_%s.json", safeTimestamp, point.ticker(), System.nanoTime());
            Path filePath = directory.resolve(fileName);

            objectMapper.writeValue(filePath.toFile(), point);
            return filePath;
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать точку истории в директорию: " + directory, e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return Collections.emptyList();
        }

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return objectMapper.readValue(path.toFile(), PricePoint.class);
                        } catch (IOException e) {
                            return null;
                        }
                    })
                    .filter(point -> point != null && point.ticker().equalsIgnoreCase(ticker))
                    .sorted(Comparator.comparing(PricePoint::timestamp))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю цен из директории: " + directory, e);
        }
    }
}