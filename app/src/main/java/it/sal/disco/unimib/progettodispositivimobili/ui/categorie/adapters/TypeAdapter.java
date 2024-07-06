package it.sal.disco.unimib.progettodispositivimobili.ui.categorie.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import it.sal.disco.unimib.progettodispositivimobili.ui.categorie.models.Comic;

public class TypeAdapter implements JsonDeserializer<Comic> {

    @Override
    public Comic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Comic comic = new Comic();
        comic.setId(json.getAsJsonObject().get("identifier").getAsString());
        comic.setTitle(json.getAsJsonObject().get("title").getAsString());
        comic.setThumbnail("https://archive.org/services/img/" + json.getAsJsonObject().get("identifier").getAsString());
        comic.setYear(json.getAsJsonObject().get("year").getAsString());
        comic.setLanguage(json.getAsJsonObject().get("language").getAsString());
        comic.setCollection(json.getAsJsonObject().get("collection").getAsString());
        comic.setSubject(json.getAsJsonObject().get("subject").getAsString());

        JsonElement descriptionElement = json.getAsJsonObject().get("description");
        if (descriptionElement.isJsonArray()) {
            List<String> descriptions = new ArrayList<>();
            for (JsonElement element : descriptionElement.getAsJsonArray()) {
                descriptions.add(element.getAsString());
            }
            comic.setDescription(descriptions);
        } else if (descriptionElement.isJsonPrimitive()) {
            comic.setDescription(descriptionElement.getAsString());
        } else {
            comic.setDescription("No Description Available");
        }

        return comic;
    }
}