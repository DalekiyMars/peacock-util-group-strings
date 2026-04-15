package com.ITQGroup;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Ошибка: Не указан путь к файлу.");
            return;
        }

        String filePath = args[0];
        long startTime = System.currentTimeMillis();

        // Подготавливаем данные
        List<String> validLines = readAndFilterUniqueLines(filePath);
        if (validLines == null) return;

        // Группируем строки DSU
        Map<Integer, List<String>> rawGroups = buildGroups(validLines);

        // Отбрасываем группы из 1 элемента и сортируем
        List<List<String>> sortedGroups = filterAndSortGroups(rawGroups);

        // Пишем в файл
        writeToFile(sortedGroups, "output.txt");

        long endTime = System.currentTimeMillis();
        System.out.println("Количество групп: " + sortedGroups.size());
        System.out.println("Время выполнения: " + (endTime - startTime) / 1000.0 + " сек.");
    }

    /**
     * Чтение файла.
     */
    private static List<String> readAndFilterUniqueLines(String filePath) {
        List<String> validLines = new ArrayList<>();
        Set<String> uniqueLinesSet = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isValid(line) && uniqueLinesSet.add(line)) {
                    validLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            return null;
        }
        return validLines;
    }

    /**
     * Основная логика поиска пересечений.
     * @return мапа групп
     */
    private static Map<Integer, List<String>> buildGroups(List<String> lines) {
        int n = lines.size();
        DSU dsu = new DSU(n);
        List<Map<String, Integer>> columnIndexes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            List<String> columns = splitLineFast(lines.get(i));

            for (int col = 0; col < columns.size(); col++) {
                String val = columns.get(col);
                if (val.isEmpty() || val.equals("\"\"")) {
                    continue;
                }

                while (columnIndexes.size() <= col) {
                    columnIndexes.add(new HashMap<>());
                }

                Map<String, Integer> currentColumnMap = columnIndexes.get(col);

                // Объединяем текущую строку с предыдущей, если значение уже было в этой колонке
                if (currentColumnMap.containsKey(val)) {
                    int previousRowId = currentColumnMap.get(val);
                    dsu.union(i, previousRowId);
                } else {
                    currentColumnMap.put(val, i);
                }
            }
        }

        // Собираем сгруппированные строки
        Map<Integer, List<String>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(lines.get(i));
        }

        return groups;
    }

    /**
     * Убираем группы из 1 элемента и сортируем
     * @param groups группы элементов
     * @return сортированый список элементов (группа - 2+ элементов)
     */
    private static List<List<String>> filterAndSortGroups(Map<Integer, List<String>> groups) {
        return groups.values().stream()
                .filter(group -> group.size() > 1)
                .sorted((g1, g2) -> Integer.compare(g2.size(), g1.size()))
                .toList();
    }

    /** Запись в файл
     * @param groups результат
     * @param outPath имя выходного файла
     */
    private static void writeToFile(List<List<String>> groups, String outPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outPath))) {
            writer.write(groups.size() + "\n");
            for (int i = 0; i < groups.size(); i++) {
                writer.write("Группа " + (i + 1) + "\n");
                for (String s : groups.get(i)) {
                    writer.write(s + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка записи: " + e.getMessage());
        }
    }

    /**
     *
     * @param line проверяемая строка
     * @return true если строка валидна
     */
    private static boolean isValid(String line) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            if (line.charAt(i) == '"') {
                boolean isStart = (i == 0 || line.charAt(i - 1) == ';');
                boolean isEnd = (i == len - 1 || line.charAt(i + 1) == ';');
                if (!isStart && !isEnd) return false;
            }
        }
        return true;
    }

    /**
     *
     * @param line быстрый split строки
     * @return разбитая строка
     */
    private static List<String> splitLineFast(String line) {
        List<String> result = new ArrayList<>();
        int start = 0, len = line.length();
        while (start <= len) {
            int end = line.indexOf(';', start);
            if (end == -1) end = len;
            String val = line.substring(start, end);
            if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
                val = val.substring(1, val.length() - 1);
            }
            result.add(val);
            start = end + 1;
        }
        return result;
    }
}