package com.PeacockTeam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
            System.err.println("Путь не задан");
            return;
        }

        String filePath = args[0];
        long startTime = System.currentTimeMillis();

        // Читаем файл, фильтруем невалидные строки
        List<String> validLines = readAndFilterUniqueLines(filePath);
        if (validLines == null) return;
        System.out.println("Valid strings: " + validLines.size());

        // Группируем через DSU
        Map<Integer, List<String>> rawGroups = buildGroups(validLines);

        // Убираем одиночные группы, сортируем по убыванию размера
        List<List<String>> sortedGroups = filterAndSortGroups(rawGroups);

        // Пишем результат в output.txt
        writeToFile(sortedGroups, "output.txt");

        long endTime = System.currentTimeMillis();
        System.out.println("Groups: " + sortedGroups.size());
        System.out.println("Time left: " + (endTime - startTime) / 1000.0 + " seconds");
    }

    /**
     * Читает файл, пропускает невалидные строки, убирает дубликаты.
     */
    private static List<String> readAndFilterUniqueLines(String filePath) {
        List<String> validLines = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty() && isValid(line) && seen.add(line)) {
                    validLines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("File read exception: " + e.getMessage());
            return null;
        }
        return validLines;
    }

    /**
     * Основная логика: строит DSU по совпадениям значений в одной колонке.
     */
    private static Map<Integer, List<String>> buildGroups(List<String> lines) {
        int n = lines.size();
        DSU dsu = new DSU(n);

        List<Map<String, Integer>> columnIndexes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String line = lines.get(i);
            int start = 0;
            int len = line.length();
            int col = 0;

            // Разбиваем строку и сразу обрабатываем каждую колонку
            while (start <= len) {
                int end = line.indexOf(';', start);
                if (end == -1) end = len;

                String val = extractValue(line, start, end);

                if (!val.isEmpty()) {
                    // Расширяем список columnIndexes при необходимости
                    while (columnIndexes.size() <= col) {
                        columnIndexes.add(new HashMap<>());
                    }

                    Map<String, Integer> colMap = columnIndexes.get(col);
                    Integer prev = colMap.putIfAbsent(val, i);
                    if (prev != null) {
                        // Значение уже встречалось в этой колонке — объединяем группы
                        dsu.union(i, prev);
                    }
                }

                start = end + 1;
                col++;
            }
        }

        // Собираем группы
        Map<Integer, List<String>> groups = new HashMap<>();
        for (int i = 0; i < n; i++) {
            int root = dsu.find(i);
            groups.computeIfAbsent(root, k -> new ArrayList<>()).add(lines.get(i));
        }

        return groups;
    }

    /**
     * Оставляем только группы с 2+ элементами, сортируем по убыванию.
     */
    private static List<List<String>> filterAndSortGroups(Map<Integer, List<String>> groups) {
        return groups.values().stream()
                .filter(g -> g.size() > 1)
                .sorted((g1, g2) -> Integer.compare(g2.size(), g1.size()))
                .toList();
    }

    /**
     * Записывает результат в файл.
     */
    private static void writeToFile(List<List<String>> groups, String outPath) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outPath), StandardCharsets.UTF_8)) {
            // Кол-во групп с более чем одним элементом — первая строка файла
            writer.write("Groups wrote: " + groups.size());
            writer.newLine();

            for (int i = 0; i < groups.size(); i++) {
                writer.newLine(); // пустая строка перед каждой группой
                writer.write("Group " + (i + 1));
                writer.newLine();
                for (String s : groups.get(i)) {
                    writer.write(s);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Writing exception: " + e.getMessage());
        }
    }

    /**
     * Вытаскивает значение колонки из подстроки.
     */
    private static String extractValue(String line, int start, int end) {
        int len = end - start;
        if (len == 0) return "";
        if (len == 2 && line.charAt(start) == '"' && line.charAt(start + 1) == '"') return "";
        if (len >= 2 && line.charAt(start) == '"' && line.charAt(end - 1) == '"') {
            return line.substring(start + 1, end - 1);
        }
        return line.substring(start, end);
    }

    /**
     * Проверка на валидность строки
     */
    private static boolean isValid(String line) {
        int len = line.length();
        for (int i = 0; i < len; i++) {
            if (line.charAt(i) == '"') {
                boolean isStart = (i == 0 || line.charAt(i - 1) == ';');
                boolean isEnd   = (i == len - 1 || line.charAt(i + 1) == ';');
                if (!isStart && !isEnd) return false;
            }
        }
        return true;
    }
}
