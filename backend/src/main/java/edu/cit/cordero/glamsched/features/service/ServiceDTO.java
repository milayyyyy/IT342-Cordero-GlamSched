package edu.cit.cordero.glamsched.features.service;

import java.util.List;

public class ServiceDTO {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String category;
    private String duration;
    private Long artistId;
    private String artistName;
    private String artistProfileImage;
    private List<String> photos;
    private long reactionCount;
    private boolean likedByMe;
    private boolean followedByMe;

    public ServiceDTO(ServiceEntity s, String artistName, String artistProfileImage) {
        this.id = s.getId();
        this.name = s.getName();
        this.description = s.getDescription();
        this.price = s.getPrice();
        this.category = s.getCategory();
        this.duration = s.getDuration();
        this.artistId = s.getArtistId();
        this.artistName = artistName;
        this.artistProfileImage = artistProfileImage;
        this.photos = s.getPhotos();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getDuration() {
        return duration;
    }

    public Long getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getArtistProfileImage() {
        return artistProfileImage;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public long getReactionCount() {
        return reactionCount;
    }

    public void setReactionCount(long reactionCount) {
        this.reactionCount = reactionCount;
    }

    public boolean isLikedByMe() {
        return likedByMe;
    }

    public void setLikedByMe(boolean likedByMe) {
        this.likedByMe = likedByMe;
    }

    public boolean isFollowedByMe() {
        return followedByMe;
    }

    public void setFollowedByMe(boolean followedByMe) {
        this.followedByMe = followedByMe;
    }
}
