package edu.cit.cordero.glamsched.shared.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationConfig {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationConfig(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        // Drop legacy NOT NULL constraints on reviews table
        runSilently("ALTER TABLE reviews ALTER COLUMN appointment_id DROP NOT NULL");
        runSilently("ALTER TABLE reviews ALTER COLUMN reviewer_id DROP NOT NULL");
        // Make service description TEXT to support long descriptions
        runSilently("ALTER TABLE services ALTER COLUMN description TYPE TEXT");
        // Drop legacy appointment_date NOT NULL constraint (entity uses separate date/time strings)
        runSilently("ALTER TABLE appointments ALTER COLUMN appointment_date DROP NOT NULL");
        // Set categories for initial services
        runSilently("UPDATE services SET category = 'Nails' WHERE id = 1 AND (category IS NULL OR category = '')");
        runSilently("UPDATE services SET category = 'Makeup' WHERE id = 2 AND (category IS NULL OR category = '')");
    }

    private void runSilently(String sql) {
        try {
            jdbcTemplate.execute(sql);
        } catch (Exception ignored) {
            // Column already nullable or table doesn't exist yet — safe to ignore
        }
    }
}
