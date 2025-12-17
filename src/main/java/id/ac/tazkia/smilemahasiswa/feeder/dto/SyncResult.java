package id.ac.tazkia.smilemahasiswa.feeder.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk hasil sinkronisasi data antara database lokal dan Feeder
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncResult<T> {

    private String entityName;
    private LocalDateTime syncTime;

    private int totalLocal;
    private int totalFeeder;

    private int matchedCount;
    private int mismatchedCount;
    private int missingInFeederCount;
    private int missingInLocalCount;
    private int skippedCount; // Jumlah yang di-skip (misal: duplikat yang menggunakan ID existing)

    @Builder.Default
    private List<SyncItem<T>> matched = new ArrayList<>();

    @Builder.Default
    private List<SyncItem<T>> mismatched = new ArrayList<>();

    @Builder.Default
    private List<T> missingInFeeder = new ArrayList<>();

    @Builder.Default
    private List<T> missingInLocal = new ArrayList<>();

    /**
     * Item sinkronisasi yang menunjukkan perbandingan data lokal vs Feeder
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncItem<T> {
        private T local;
        private T feeder;
        private List<String> differences;
    }

    public boolean isSynced() {
        return mismatchedCount == 0 && missingInFeederCount == 0 && missingInLocalCount == 0;
    }

    public String getSummary() {
        if (isSynced()) {
            return String.format("✅ %s synchronized: %d records matched", entityName, matchedCount);
        } else {
            return String.format(
                    "⚠️ %s NOT synchronized: %d matched, %d mismatched, %d missing in Feeder, %d missing in Local",
                    entityName, matchedCount, mismatchedCount, missingInFeederCount, missingInLocalCount);
        }
    }
}
