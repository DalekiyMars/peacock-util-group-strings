package com.PeacockTeam;

import java.util.Objects;

public class DSU {
    private final int[] parent;
    private final int[] rank;

    public DSU(int size) {
        parent = new int[size];
        rank = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    // Поиск корня
    public int find(int i) {
        if (Objects.equals(parent[i], i)) {
            return i;
        }
        // Перевешиваем элементы напрямую к корню
        return parent[i] = find(parent[i]);
    }

    // Объединение двух множеств
    public void union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);

        if (rootI != rootJ) {
            if (rank[rootI] < rank[rootJ]) {
                parent[rootI] = rootJ;
            } else if (rank[rootI] > rank[rootJ]) {
                parent[rootJ] = rootI;
            } else {
                parent[rootJ] = rootI;
                rank[rootI]++;
            }
        }
    }
}
