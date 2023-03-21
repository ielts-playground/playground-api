package org.ielts.playground.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostPaginationRequest {
    private Long postId;
    private String title;
    private String author;
    private Integer page;
    private Integer pageSize;
}
