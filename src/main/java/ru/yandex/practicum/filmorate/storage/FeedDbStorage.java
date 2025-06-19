package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.FeedEvent;
import ru.yandex.practicum.filmorate.model.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

@Repository
public class FeedDbStorage implements FeedStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FeedDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(FeedEvent event) {
        Integer nextId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(EVENT_ID), 0) + 1 FROM FEED", Integer.class
        );
        event.setEventId(nextId);

        event.setTimestamp(Instant.now().toEpochMilli());
        String sql = "INSERT INTO feed (event_id, timestamp, user_id, event_type, operation, entity_id)" +
                " VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql,
                event.getEventId(),
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
    }

    @Override
    public List<FeedEvent> getFeedByUserId(Integer userId) {
        if (!userExistsById(userId)) {
            throw new NotFoundException("User with id " + userId + " does not exist");
        }
        String sql = "SELECT * FROM feed WHERE user_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, this::mapRowToFeedEvent, userId);
    }

    private FeedEvent mapRowToFeedEvent(ResultSet rs, int rowNum) throws SQLException {
        FeedEvent event = new FeedEvent();
        event.setEventId(rs.getInt("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getInt("entity_id"));
        return event;
    }

    private boolean userExistsById(Integer userId) {
        String sql = "SELECT COUNT(*) FROM USERS WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count > 0;
    }
}
