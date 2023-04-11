package org.ielts.playground.controller;

import java.io.IOException;
import java.util.Collection;

import javax.validation.constraints.NotNull;

import org.ielts.playground.model.response.DisplayData;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.ielts.playground.common.constant.CachingConstants;
import org.ielts.playground.common.constant.PathConstants;
import org.ielts.playground.model.request.PostCreationRequest;
import org.ielts.playground.model.request.PostPaginationRequest;
import org.ielts.playground.model.response.PostInfoResponse;
import org.ielts.playground.service.PostService;
import org.ielts.playground.utils.CSV;

@Profile({ "local" })
@RestController
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Cacheable(key = "#postId", unless = "#result == null", cacheNames = CachingConstants.POST_VIEW_CACHE_NAME)
    @GetMapping(PathConstants.API_POSTS_VIEW_URL)
    public PostInfoResponse view(@PathVariable @NotNull Long postId) {
        return this.postService.view(postId);
    }

    @Cacheable(key = "{ #title, #author, #page.pageNumber, #page.pageSize }", cacheNames = CachingConstants.POST_SEARCH_CACHE_NAME)
    @GetMapping(PathConstants.API_POSTS_SEARCH_URL)
    public Collection<PostInfoResponse> search(
            @RequestParam(name = "post_id", required = false) Long postId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            Pageable page) {
        return this.postService.search(PostPaginationRequest.builder()
                .postId(postId)
                .title(title)
                .author(author)
                .page(page.getPageNumber())
                .pageSize(page.getPageSize()).build());
    }

    @CachePut(key = "#result.id", cacheNames = CachingConstants.POST_VIEW_CACHE_NAME)
    @PostMapping(PathConstants.API_POSTS_CREATION_URL)
    public PostInfoResponse create(@RequestBody @NotNull PostCreationRequest request) {
        return this.postService.create(request);
    }

    @CacheEvict(key = "#postId", cacheNames = CachingConstants.POST_VIEW_CACHE_NAME)
    @DeleteMapping(PathConstants.API_POSTS_DELETION_URL)
    public void remove(@PathVariable @NotNull Long postId) {
        this.postService.remove(postId);
    }

    @PostMapping(PathConstants.API_POSTS_UPLOAD_URL)
    public void upload(@RequestParam MultipartFile file) throws IOException {
        CSV csv = CSV.builder()
                .hasHeader(true)
                .delimiter(",")
                .build()
                .fromText(new String(file.getBytes()));
        CSV.HeaderValueEntityMapper<PostCreationRequest> mapper = post -> (header, value) -> {
            if (header.equalsIgnoreCase("title")) {
                post.setTitle(value);
            } else if (header.equalsIgnoreCase("content")) {
                post.setContent(value);
            }
        };
        this.postService.create(csv.as(PostCreationRequest::new, mapper));
    }

    @GetMapping(PathConstants.API_POSTS_GENERATE_URL)
    public Collection<String> generate(
        @RequestParam(required = false, defaultValue = "true") Boolean async,
        @RequestParam(required = false) String generator,
        @RequestParam(required = false, defaultValue = "1") Integer total
    ) {
        return this.postService.generate(async, total, generator);
    }
}
