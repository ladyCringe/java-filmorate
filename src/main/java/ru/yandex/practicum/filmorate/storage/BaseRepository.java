package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> mapper;

    @Autowired
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected Optional<T> findOne(String query, Object... params) {
        try {
            T result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected <V> V findOne(String query, Class<V> tClass, Object... params) {
        return jdbc.queryForObject(query, tClass, params);
    }

    protected List<T> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    protected List<T> findManyParameterSource(String query, SqlParameterSource parameters) {
        return namedParameterJdbcTemplate.query(query, parameters, mapper);
    }

    protected <K, V> Map<K, V> findMap(String query, SqlParameterSource parameters, ResultSetExtractor<Map<K, V>> extractor) {
        return namedParameterJdbcTemplate.query(query, parameters, extractor);
    }

    protected boolean delete(String query, long id) {
        int rowsDeleted = jdbc.update(query, id);
        return rowsDeleted > 0;
    }

    protected void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
    }

    protected long insert(String query, Object... params) {
        try {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbc.update(connection -> {
                PreparedStatement ps = connection
                        .prepareStatement(query, new String[]{"id"});
                for (int idx = 0; idx < params.length; idx++) {
                    ps.setObject(idx + 1, params[idx]);
                }
                return ps;
            }, keyHolder);

            // Возвращаем id нового пользователя
            return Objects.requireNonNull(keyHolder.getKey()).longValue();
        } catch (DataAccessException e) {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    protected void insertBatch(String query, BatchPreparedStatementSetter batchPreparedStatementSetter) {
        int[] rowsInserted = jdbc.batchUpdate(query, batchPreparedStatementSetter);
    }

    protected void insertParameterSource(String query, SqlParameterSource parameters) {
        int rowsInsert = namedParameterJdbcTemplate.update(query, parameters);
    }

    protected void updateParameterSource(String query, SqlParameterSource parameters) {
        int rowsInsert = namedParameterJdbcTemplate.update(query, parameters);
    }

    protected void deleteParameterSource(String query, SqlParameterSource parameters) {
        int rowsInsert = namedParameterJdbcTemplate.update(query, parameters);
    }
}
