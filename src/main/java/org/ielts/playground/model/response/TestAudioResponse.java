package org.ielts.playground.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestAudioResponse {
    private Long test;
    private String name;
    private MediaType type;
    private byte[] data;
}
