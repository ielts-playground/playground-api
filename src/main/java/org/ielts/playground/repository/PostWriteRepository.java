package org.ielts.playground.repository;

import java.util.Collection;
import java.util.Optional;

import javax.persistence.Table;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import org.ielts.playground.model.entity.Post;

@Repository
public class PostWriteRepository extends AbstractWriteRepository<Post> {

    protected PostWriteRepository(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void saveAll(Collection<Post> posts) {
        Optional.ofNullable(Post.class.getAnnotation(Table.class))
                .ifPresent(table -> {
                    String sql = String.format(
                            "INSERT INTO %s (title, content, author) VALUES %s",
                            table.name(), duplicateAndJoin(posts.size(), "(?, ?, ?)", ", "));

                    jdbcTemplate.update(sql, ps -> {
                        int pos = 1; // param index starts with 1
                        for (Post post : posts) {
                            ps.setString(pos++, post.getTitle());
                            ps.setString(pos++, post.getContent());
                            ps.setString(pos++, post.getAuthor().getUsername());
                        }
                    });
                    // REF: insert into database using batch (https://www.baeldung.com/jpa-hibernate-batch-insert-update).
                });
    }
}
