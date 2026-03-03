package org.example.flyora_backend.service;

import org.example.flyora_backend.model.NewsArticle;
import org.example.flyora_backend.repository.NewsArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsArticleService {

    @Autowired
    private NewsArticleRepository newsArticleRepository;

    public List<NewsArticle> getAllArticles() {
        return newsArticleRepository.findAll();
    }

    public Optional<NewsArticle> getOneArticle(Integer id) {
        return newsArticleRepository.findById(id);
    }

    public NewsArticle addArticle(NewsArticle article) {
        return newsArticleRepository.save(article);
    }

    public void deleteArticle(Integer id) {
        newsArticleRepository.deleteById(id);
    }
}
