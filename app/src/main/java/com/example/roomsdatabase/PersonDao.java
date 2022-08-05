package com.example.roomsdatabase;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PersonDao {
    @Query("Select * from person")
    List<Person> getPersonList();

    @Insert
    void insertPerson(Person person);

    @Update
    void updatePerson(Person person);

    @Delete
    void deletePerson(Person person);

    @Query("DELETE FROM person WHERE id =:id")
    void deletePersonById(int id);

    @Query("SELECT * FROM person WHERE name LIKE :name || '%'")
    List<Person> loadPersonByName(String name);

    @Query("SELECT * FROM person WHERE city LIKE :city || '%'")
    List<Person> loadPersonByCity(String city);
}
