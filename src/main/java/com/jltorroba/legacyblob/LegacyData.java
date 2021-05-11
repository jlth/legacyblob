package com.jltorroba.legacyblob;

import java.util.Arrays;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

@Entity
public class LegacyData {

    @Transient
    private StorageService service;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Lob
    private byte[] data;

    @Transient
    private byte[] dataCache;

    @Transient
    private byte[] source;

    public LegacyData() {
        service = StorageServiceFactory.load();
    }

    public Integer getId () {
        return id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    public byte[] getData () {
        return data;
    }

    public void setData (byte[] data) {
        this.data = data;
    }

    @PostLoad
    private void loadFromStorage () {
        source = data;
        data = service.get(source);
        dataCache = data;
    }

    @PostPersist
    @PostUpdate
    private void loadFromCache () {
        data = dataCache;
    }

    @PrePersist
    private void persistToStorage () {
        if (Arrays.equals(this.dataCache, data)) {
            return;
        }
        dataCache = data;
        source = service.save(data);
        data = source;
    }

    @PreUpdate
    private void updateToStorage () {
        if (Arrays.equals(this.dataCache, data)) {
            return;
        }
        dataCache = data;
        data = service.update(source, data);
    }



}
