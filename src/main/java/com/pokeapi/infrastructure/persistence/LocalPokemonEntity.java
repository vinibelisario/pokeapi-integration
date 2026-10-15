package com.pokeapi.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "local_pokemon")
public class LocalPokemonEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 1000)
    private String description;

    @Column(name = "localized_name", length = 100)
    private String localizedName;

    @Column(length = 100)
    private String region;

    @Column(name = "internal_tags", length = 500)
    private String internalTags;

    protected LocalPokemonEntity() {
        // required by JPA
    }

    public LocalPokemonEntity(Integer id, String name, String imageUrl, String description,
                              String localizedName, String region, String internalTags) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.description = description;
        this.localizedName = localizedName;
        this.region = region;
        this.internalTags = internalTags;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String getLocalizedName() { return localizedName; }
    public String getRegion() { return region; }
    public String getInternalTags() { return internalTags; }
}