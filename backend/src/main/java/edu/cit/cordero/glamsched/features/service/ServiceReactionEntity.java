package edu.cit.cordero.glamsched.features.service;

import jakarta.persistence.*;

@Entity
@Table(name = "service_reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "service_id", "client_id" })
})
public class ServiceReactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    public ServiceReactionEntity() {
    }

    public ServiceReactionEntity(Long serviceId, Long clientId) {
        this.serviceId = serviceId;
        this.clientId = clientId;
    }

    public Long getId() {
        return id;
    }

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
