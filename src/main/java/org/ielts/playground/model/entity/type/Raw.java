package org.ielts.playground.model.entity.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Raw extends Text {
    private String value;

    @Override
    public String toString() {
        return this.value;
    }
}
