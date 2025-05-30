package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ServerException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        if (user.getId() != null) {
            throw new ServerException("User id already exists");
        }
        Integer nextId = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(id), 0) + 1 FROM USERS", Integer.class
        );
        user.setId(nextId);
        String sql = "INSERT INTO users (id, email, login, name, birthday) VALUES " +
                "(?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getId(), user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!existsById(user.getId())) {
            throw new NotFoundException("User not found");
        }
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM users", this::mapRowToUser);
    }

    @Override
    public User getUserById(Integer id) {
        if (!existsById(id)) {
            throw new NotFoundException("User not found");
        }
        User user = jdbcTemplate.queryForObject("SELECT * FROM users WHERE id = ?", this::mapRowToUser, id);
        if (user != null) {
            user.getFriends().addAll(getFriends(id).stream().map(User::getId).toList());
        }
        return user;
    }

    public List<User> getFriends(int userId) {
        return jdbcTemplate.query(
                "SELECT u.* FROM users u " +
                        "JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?",
                this::mapRowToUser, userId);
    }


    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    @Override
    public void addFriend(Integer userId, Integer friendId) {
        getUserById(userId);
        getUserById(friendId);

        Boolean alreadyAdded = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) > 0 FROM friendships WHERE user_id = ? AND friend_id = ?",
                Boolean.class, friendId, userId
        );

        boolean confirmed = Boolean.TRUE.equals(alreadyAdded);

        jdbcTemplate.update("INSERT INTO friendships (user_id, friend_id, confirmed) VALUES (?, ?, ?)",
                userId, friendId, confirmed);

        if (confirmed) {
            jdbcTemplate.update("UPDATE friendships SET confirmed = true WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
        }
    }

    @Override
    public void removeFriend(Integer userId, Integer friendId) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ? AND friend_id = ?", userId, friendId);

        jdbcTemplate.update("UPDATE friendships SET confirmed = false WHERE user_id = ? AND friend_id = ?",
                friendId, userId);
    }

    private boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count > 0;
    }
}
