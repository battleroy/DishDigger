package by.bsu.battleroy.pmvs_lab03.model;

import android.net.Uri;

public class Recipe {

    private String id;
    private String title;
    private Uri imageUri;

    public Recipe(String id, String title, Uri imageUri) {
        this.id = id;
        this.title = title;
        this.imageUri = imageUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}
