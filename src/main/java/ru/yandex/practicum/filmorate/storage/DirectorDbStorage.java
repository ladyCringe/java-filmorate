package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.util.Reflection;

import java.util.Collection;
import java.util.Optional;

@Repository
@Qualifier("DirectorDbStorage")
public class DirectorDbStorage extends BaseRepository<Director> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors;";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?;";
    private static final String INSERT_DIRECTOR_QUERY = "INSERT INTO directors (name) VALUES (?);";
    private static final String UPDATE_QUERY = "UPDATE directors" +
            " SET name = ?" +
            " WHERE id = ?";
    private static final String DELETE_DIRECTOR_FILMS_QUERY = "DELETE FROM film_director WHERE director_id = ?;";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE id = ?;";

    private static final Logger log = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(DirectorDbStorage.class);

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Director add(Director director) {
        // сохраняем данные о новом режиссере в БД приложения
        long id = insert(
                INSERT_DIRECTOR_QUERY,
                director.getName());
        director.setId(id);

        log.info("Добавлена информация о новом режиссере {}.", director);

        return getById(director.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + director.getId() + " не найден."));
    }

    public Director update(Director newDirector) {
        // получаем данные по режиссеру с id из БД (oldDirector)
        Director oldDirector = getById(newDirector.getId())
                .orElseThrow(() -> new NotFoundException("Режиссер с id = " + newDirector.getId() + " не найден."));

        // обновляем содержимое в объекте oldDirector
        BeanUtils.copyProperties(newDirector, oldDirector, Reflection.getIgnoreProperties(newDirector));

        // обновляем данные о режиссере в БД (oldDirector)
        update(
                UPDATE_QUERY,
                oldDirector.getName(),
                oldDirector.getId());

        log.info("Обновлены данные режиссера {}.", oldDirector);

        return oldDirector;
    }

    @Transactional
    public Director delete(Director director) {
        // Делаю удаление из двух таблиц, предполагая, что каскадное удаление может быть снято с таблицы Film_Director

        // удаляем данные о фильмах режиссера в БД
        delete(DELETE_DIRECTOR_FILMS_QUERY, director.getId()); // удаляем записи о текущих фильмах режиссера

        // удаляем данные о режиссере из БД.
        delete(DELETE_DIRECTOR_QUERY, director.getId());

        return director;
    }

    public Optional<Director> getById(Long id) {
        log.info("Запрошена информация по режиссеру с {}.", id);

        // получаем данные из БД о фильме.
        Optional<Director> optionalDirector = findOne(FIND_BY_ID_QUERY, id);

        return optionalDirector;
    }

    public Collection<Director> findAll() {
        log.info("Получен список режиссеров.");

        return findMany(FIND_ALL_QUERY);
    }
}
