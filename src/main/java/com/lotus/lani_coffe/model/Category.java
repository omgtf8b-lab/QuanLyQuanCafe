package com.lotus.lani_coffe.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 255, message = "Tên danh mục không được vượt quá 255 ký tự")
    @Column(nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Size(max = 255, message = "Slug không được vượt quá 255 ký tự")
    @Column(unique = true)
    private String slug;

    private String icon; // Icon CSS hoặc SVG class

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;
}
