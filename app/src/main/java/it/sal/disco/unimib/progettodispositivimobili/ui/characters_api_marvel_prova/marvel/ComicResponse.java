package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.marvel;

import com.google.gson.annotations.SerializedName;
import java.util.List;

    public class ComicResponse {
        @SerializedName("data")
        private ComicData data;

        public ComicData getData() {
            return data;
        }

        public void setData(ComicData data) {
            this.data = data;
        }

        public static class ComicData {
            @SerializedName("results")
            private List<Comix> results;

            public List<Comix> getResults() {
                return results;
            }

            public void setResults(List<Comix> results) {
                this.results = results;
            }
        }
}