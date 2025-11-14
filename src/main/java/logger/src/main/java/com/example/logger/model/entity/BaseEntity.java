package com.example.logger.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "data_criacao", updatable = false)
    private Instant dataCriacao;

    @LastModifiedDate
    @Column(name = "data_modificacao")
    private Instant dataModificacao;

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public Instant getDataModificacao() {
        return dataModificacao;
    }
}
