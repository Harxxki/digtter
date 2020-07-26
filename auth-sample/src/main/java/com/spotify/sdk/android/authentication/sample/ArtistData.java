package com.spotify.sdk.android.authentication.sample;

public class ArtistData {
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    private final String id;
    private final String name;

    public ArtistData(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
