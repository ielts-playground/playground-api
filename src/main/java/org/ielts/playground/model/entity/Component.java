package org.ielts.playground.model.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.ielts.playground.common.enumeration.ComponentType;
import org.ielts.playground.model.entity.type.Map;
import org.ielts.playground.model.entity.type.Size;
import org.ielts.playground.model.entity.type.Text;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "component")
public class Component extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "part_id")
    private Long partId;

    @Column(name = "position")
    private String position;

    @Column(name = "type")
    private ComponentType type;

    @Column(name = "kei")
    private String kei;

    @Column(name = "value")
    private Text value;

    @Column(name = "size")
    private Size size;

    @Column(name = "options")
    private Map options;

    @Transient
    private Long partNumber;

    public static Component includePartNumber(
            @NotNull Component component,
            Long partNumber) {
        component.setPartNumber(partNumber);
        return component;
    }
}
