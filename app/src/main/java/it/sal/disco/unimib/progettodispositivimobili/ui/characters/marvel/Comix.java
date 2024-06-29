package it.sal.disco.unimib.progettodispositivimobili.ui.characters.marvel;

import com.google.gson.annotations.SerializedName;

public class Comix {
    @SerializedName("id")
    private int id;
    @SerializedName("title")
    private String title;
    @SerializedName("thumbnail")
    private Thumbnail thumbnail;

    // getters and setters


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public static class Thumbnail {
        @SerializedName("path")
        private String path;
        @SerializedName("extension")
        private String extension;

        // getters and setters

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getExtension() {
            return extension;
        }

        public void setExtension(String extension) {
            this.extension = extension;
        }
    }

}
