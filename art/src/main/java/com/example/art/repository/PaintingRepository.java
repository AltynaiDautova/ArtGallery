package com.example.art.repository;

import com.example.art.model.Painting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PaintingRepository extends JpaRepository<Painting, Long>, JpaSpecificationExecutor<Painting> {

    @Query("SELECT DISTINCT p.creationYear FROM Painting p WHERE p.creationYear IS NOT NULL ORDER BY p.creationYear")
    List<Integer> findDistinctCreationYears();

    @Query("SELECT DISTINCT p.genre FROM Painting p WHERE p.genre IS NOT NULL")
    List<String> findDistinctGenres();

    @Query("SELECT DISTINCT p.material FROM Painting p WHERE p.material IS NOT NULL")
    List<String> findDistinctMaterials();
}
