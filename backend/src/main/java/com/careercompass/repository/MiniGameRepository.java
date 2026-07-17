package com.careercompass.repository;

import com.careercompass.entity.MiniGame;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameRepository extends JpaRepository<MiniGame, UUID> {

    List<MiniGame> findByActiveTrueOrderByTitleAsc();
}
