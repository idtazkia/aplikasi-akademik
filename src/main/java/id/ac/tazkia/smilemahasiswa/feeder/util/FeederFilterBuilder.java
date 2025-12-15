package id.ac.tazkia.smilemahasiswa.feeder.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class untuk membangun filter string untuk Feeder API.
 * 
 * Feeder API menggunakan format filter string seperti SQL WHERE clause.
 * Class ini membantu membangun filter dengan cara yang lebih aman dan readable.
 * 
 * Usage:
 * 
 * <pre>
 * String filter = FeederFilterBuilder.builder()
 *         .eq("id_prodi", prodiId)
 *         .like("nama_mahasiswa", nama)
 *         .build();
 * </pre>
 */
public class FeederFilterBuilder {

    private final List<String> conditions = new ArrayList<>();

    private FeederFilterBuilder() {
    }

    /**
     * Create a new filter builder
     */
    public static FeederFilterBuilder builder() {
        return new FeederFilterBuilder();
    }

    /**
     * Add an equality condition: field = 'value'
     * 
     * @param field the field name
     * @param value the value to match (will be escaped)
     * @return this builder
     */
    public FeederFilterBuilder eq(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s='%s'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a LIKE condition: field like '%value%'
     * 
     * @param field the field name
     * @param value the value to search (will be escaped and wrapped with %)
     * @return this builder
     */
    public FeederFilterBuilder like(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s like '%%%s%%'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a starts-with LIKE condition: field like 'value%'
     * 
     * @param field the field name
     * @param value the value to search (will be escaped)
     * @return this builder
     */
    public FeederFilterBuilder startsWith(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s like '%s%%'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add an ends-with LIKE condition: field like '%value'
     * 
     * @param field the field name
     * @param value the value to search (will be escaped)
     * @return this builder
     */
    public FeederFilterBuilder endsWith(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s like '%%%s'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a not-equal condition: field != 'value'
     * 
     * @param field the field name
     * @param value the value to not match (will be escaped)
     * @return this builder
     */
    public FeederFilterBuilder ne(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s!='%s'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a greater-than condition: field > 'value'
     * 
     * @param field the field name
     * @param value the value to compare
     * @return this builder
     */
    public FeederFilterBuilder gt(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s>'%s'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a less-than condition: field < 'value'
     * 
     * @param field the field name
     * @param value the value to compare
     * @return this builder
     */
    public FeederFilterBuilder lt(String field, String value) {
        if (field != null && value != null && !value.isEmpty()) {
            conditions.add(String.format("%s<'%s'", field, escapeValue(value)));
        }
        return this;
    }

    /**
     * Add a raw condition (use with caution, no escaping)
     * 
     * @param condition the raw condition string
     * @return this builder
     */
    public FeederFilterBuilder raw(String condition) {
        if (condition != null && !condition.isEmpty()) {
            conditions.add(condition);
        }
        return this;
    }

    /**
     * Build the filter string, joining conditions with AND
     * 
     * @return the filter string, or null if no conditions
     */
    public String build() {
        if (conditions.isEmpty()) {
            return null;
        }
        return String.join(" And ", conditions);
    }

    /**
     * Build the filter string, joining conditions with OR
     * 
     * @return the filter string, or null if no conditions
     */
    public String buildOr() {
        if (conditions.isEmpty()) {
            return null;
        }
        return String.join(" Or ", conditions);
    }

    /**
     * Escape single quotes in value to prevent injection
     */
    private String escapeValue(String value) {
        return value.replace("'", "''");
    }

    // ============== Static helper methods for simple cases ==============

    /**
     * Create a simple equality filter: field = 'value'
     */
    public static String eq(String field, String value, boolean dummy) {
        if (field == null || value == null || value.isEmpty()) {
            return null;
        }
        return String.format("%s='%s'", field, value.replace("'", "''"));
    }

    /**
     * Create a simple LIKE filter: field like '%value%'
     */
    public static String like(String field, String value, boolean dummy) {
        if (field == null || value == null || value.isEmpty()) {
            return null;
        }
        return String.format("%s like '%%%s%%'", field, value.replace("'", "''"));
    }

    /**
     * Join multiple filter conditions with AND
     */
    public static String and(String... conditions) {
        List<String> validConditions = new ArrayList<>();
        for (String c : conditions) {
            if (c != null && !c.isEmpty()) {
                validConditions.add(c);
            }
        }
        return validConditions.isEmpty() ? null : String.join(" And ", validConditions);
    }

    /**
     * Join multiple filter conditions with OR
     */
    public static String or(String... conditions) {
        List<String> validConditions = new ArrayList<>();
        for (String c : conditions) {
            if (c != null && !c.isEmpty()) {
                validConditions.add(c);
            }
        }
        return validConditions.isEmpty() ? null : String.join(" Or ", validConditions);
    }
}
