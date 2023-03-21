package org.ielts.playground.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import org.ielts.playground.common.constant.AuthorityConstants;
import org.ielts.playground.common.constant.DataResultConstants;
import org.ielts.playground.common.exception.InternalServerException;
import org.ielts.playground.common.exception.NotFoundException;
import org.ielts.playground.common.exception.UnauthorizedRequestException;
import org.ielts.playground.model.entity.Post;
import org.ielts.playground.model.entity.User;
import org.ielts.playground.model.request.PostCreationRequest;
import org.ielts.playground.model.request.PostPaginationRequest;
import org.ielts.playground.model.response.PostInfoResponse;
import org.ielts.playground.repository.PostRepository;
import org.ielts.playground.repository.PostWriteRepository;
import org.ielts.playground.repository.UserRepository;
import org.ielts.playground.service.ContentGeneratingService;
import org.ielts.playground.service.PostService;
import org.ielts.playground.service.factory.ContentGeneratingServiceFactory;
import org.ielts.playground.utils.SecurityUtils;

@Service
public class PostServiceImpl implements PostService {

    private final SecurityUtils securityUtils;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostWriteRepository postWriteRepository;
    private final ContentGeneratingServiceFactory contentGeneratingServiceFactory;

    public PostServiceImpl(
            SecurityUtils securityUtils,
            PostRepository postRepository,
            UserRepository userRepository,
            PostWriteRepository postWriteRepository,
            ContentGeneratingServiceFactory contentGeneratingServiceFactory) {
        this.securityUtils = securityUtils;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.postWriteRepository = postWriteRepository;
        this.contentGeneratingServiceFactory = contentGeneratingServiceFactory;
    }

    @Override
    public PostInfoResponse create(PostCreationRequest request) {
        Optional<User> user = Optional.ofNullable(securityUtils.getLoggedUsername())
                .flatMap(this.userRepository::findByUsername);
        if (user.isPresent()) {
            Post saved = this.postRepository.save(Post.builder()
                    .author(user.get())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .build());
            return this.toPostInfoResponse(saved);
        }
        return null;
    }

    @Override
    public void create(Collection<PostCreationRequest> posts) {
        Optional<User> user = Optional.ofNullable(this.securityUtils.getLoggedUsername())
                .flatMap(this.userRepository::findByUsername);
        if (user.isPresent() && !posts.isEmpty()) {
            this.postWriteRepository.saveAll(posts.stream()
                    .map(post -> Post.builder()
                            .author(user.get())
                            .title(post.getTitle())
                            .content(post.getContent())
                            .build())
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public PostInfoResponse view(Long postId) {
        return this.postRepository.findById(postId)
                .map(this::toPostInfoResponse)
                .orElseThrow(NotFoundException::new);
    }

    @Override
    public Collection<PostInfoResponse> search(PostPaginationRequest request) {
        return this.postRepository.search(
                PageRequest.of(request.getPage(), request.getPageSize()),
                request.getPostId(),
                request.getTitle(),
                request.getAuthor()).stream()
                .map(this::toPostInfoResponse).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void remove(Long postId) {
        if (this.securityUtils.getLoggedAuthorities().contains(AuthorityConstants.ROLE_ADMIN)) {
            this.postRepository.deleteById(postId);
        } else {
            if (Optional.ofNullable(this.securityUtils.getLoggedUsername())
                    .map(username -> this.postRepository
                            .deleteByIdAndAuthor(postId, username) == DataResultConstants.MODIFIED_FAILED)
                    .orElse(true)) {
                throw new UnauthorizedRequestException();
            }
        }
    }

    @Override
    public Collection<String> generate(boolean async, int total, String generator) {
        return async
                ? this.generateUsingCompletableFuture(total, generator)
                : this.generateNormally(total, generator);
    }

    private Collection<String> generateNormally(int total, String generator) {
        ContentGeneratingService contentGeneratingService = contentGeneratingServiceFactory.getService(generator);
        List<String> generatedContents = new ArrayList<>();
        for (int count = 0; count < total; count++) {
            generatedContents.add(contentGeneratingService.generate());
        }
        return generatedContents;
    }

    @SuppressWarnings("java:S2142")
    private Collection<String> generateUsingCompletableFuture(int total, String generator) {
        ContentGeneratingService contentGeneratingService = contentGeneratingServiceFactory.getService(generator);
        List<CompletableFuture<String>> completableFutures = new ArrayList<>();
        for (int count = 0; count < total; count++) {
            completableFutures.add(CompletableFuture.supplyAsync(contentGeneratingService::generate));
        }
        try {
            return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[total]))
                    .thenApply(
                            v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()))
                    .get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new InternalServerException(ex.getMessage());
        }
    }

    private PostInfoResponse toPostInfoResponse(Post post) {
        return PostInfoResponse.builder()
                .id(post.getId())
                .author(post.getAuthor().getUsername())
                .title(post.getTitle())
                .content(post.getContent())
                .build();
    }
}
