package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa_ratings ORDER BY id", this::mapRowToMpa);
    }

    @Override
    public MpaRating findById(int id) {
        if (!existsById(id)) {
            throw new NotFoundException("mpa with id " + id + " not found");
        }
        return jdbcTemplate.queryForObject("SELECT * FROM mpa_ratings WHERE id = ?", this::mapRowToMpa, id);
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(rs.getInt("id"), rs.getString("name"));
    }

    private boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM MPA_RATINGS WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }
}