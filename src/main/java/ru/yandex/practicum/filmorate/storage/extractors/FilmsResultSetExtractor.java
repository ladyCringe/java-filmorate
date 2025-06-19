package ru.yandex.practicum.filmorate.storage.extractors;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmsResultSetExtractor implements ResultSetExtractor<List<Film>> {
    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException, DataAccessException {

        LinkedHashMap<Integer, Film> filmLinkedHashMap = new LinkedHashMap<>(); //кэш для объектов класса Film
        HashMap<Integer, MpaRating> mpaRatingHashMap = new HashMap<>(); //кэш для объектов класса MpaRating
        HashMap<Integer, Genre> genreHashMap = new HashMap<>(); //кэш для объектов класса Genre
        HashMap<Long, Director> directorHashMap = new HashMap<>();  //кэш для объектов класса Director
        //кэш для User (лайки фильму) не используем, т.к. пушим в Films.likes id пользователя

        while (rs.next()) {

            Integer idFilm = rs.getInt("f_id");
            Film film = filmLinkedHashMap.get(idFilm);
            if (film == null) {
                // новый фильм
                film = new Film();
                film.setId(idFilm);
                film.setName(rs.getString("f_name"));
                film.setDescription(rs.getString("f_description"));
                film.setReleaseDate(rs.getDate("f_release_date").toLocalDate());
                film.setDuration(rs.getInt("f_duration"));

                Integer idMpa = rs.getInt("mpa_id");
                if (!rs.wasNull()) {
                    MpaRating mpaRating = mpaRatingHashMap.get(idMpa);
                    if (mpaRating == null) {
                        mpaRating = new MpaRating(idMpa, rs.getString("mr_name"));

                        mpaRatingHashMap.put(idMpa, mpaRating);
                    }
                    film.setMpa(mpaRating);
                }

                filmLinkedHashMap.put(idFilm, film);
            }

            Long idDirector = rs.getLong("director_id");
            if (!rs.wasNull()) {
                Director director = directorHashMap.get(idDirector);
                if (director == null) {
                    director = new Director();
                    director.setId(idDirector);
                    director.setName(rs.getString("director_name"));

                    directorHashMap.put(idDirector, director);
                }

                film.getDirectors().add(director);
            }

            Integer idGenre = rs.getInt("genre_id");
            if (!rs.wasNull()) {
                Genre genre = genreHashMap.get(idGenre);
                if (genre == null) {
                    genre = new Genre(idGenre, rs.getString("genre_name"));

                    genreHashMap.put(idGenre, genre);
                }

                film.getGenres().add(genre);
            }

            Integer idUser = rs.getInt("likes_user_id");
            if (!rs.wasNull()) film.getLikes().add(idUser);
        }

        return new ArrayList<>(filmLinkedHashMap.values());
    }
}
