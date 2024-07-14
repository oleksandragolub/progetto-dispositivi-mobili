package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.Model;

import java.util.List;

public class Result {
    private int id;
    private String name;
    private String description;
    private Thumbnail thumbnail;
    private List<Url> urls;
    private ComicList comics;
    private StoryList stories;
    private EventList events;
    private SeriesList series;
    private String modified;

    // Getters and setters for all fields
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<Url> getUrls() {
        return urls;
    }

    public void setUrls(List<Url> urls) {
        this.urls = urls;
    }

    public ComicList getComics() {
        return comics;
    }

    public void setComics(ComicList comics) {
        this.comics = comics;
    }

    public StoryList getStories() {
        return stories;
    }

    public void setStories(StoryList stories) {
        this.stories = stories;
    }

    public EventList getEvents() {
        return events;
    }

    public void setEvents(EventList events) {
        this.events = events;
    }

    public SeriesList getSeries() {
        return series;
    }

    public void setSeries(SeriesList series) {
        this.series = series;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }
}
