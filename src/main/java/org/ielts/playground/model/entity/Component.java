package org.ielts.playground.model.entity;


import lombok.*;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "component")
public class Component extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;


    @Column(name = "part_id")
    private Long partId;

    @Column(name = "position")
    private String position;

    @Column(name = "type")
    private String type;

    @Column(name = "kei")
    private String kei;

    @Column(name = "value")
    private String value;

    @Column(name = "size")
    private String size;

    @Column(name = "options")
    private String options;

}
