package edu.cit.cordero.glamsched.features.auth;

import jakarta.persistence.*;

@Entity
@Table(name = "follows", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"client_id", "artist_id"})
})
public class Follow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    public Follow() {}

    public Follow(Long clientId, Long artistId) {
        this.clientId = clientId;
        this.artistId = artistId;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public Long getArtistId() { return artistId; }
    public void setArtistId(Long artistId) { this.artistId = artistId; }
}
