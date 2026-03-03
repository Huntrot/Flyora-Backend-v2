package org.example.flyora_backend.repository;

import java.util.List;

import org.example.flyora_backend.model.NewsArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Integer> {
    List<NewsArticle> findAllByOrderByCreatedAtDesc();
}