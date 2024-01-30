package it.sal.disco.unimib.progettodispositivimobili.util;

import it.sal.disco.unimib.progettodispositivimobili.model.ComicDto;
import it.sal.disco.unimib.progettodispositivimobili.model.ComicsDto;
import it.sal.disco.unimib.progettodispositivimobili.model.ComicsQuery;
import it.sal.disco.unimib.progettodispositivimobili.model.MarvelResponse;
import java.util.Map;
import retrofit2.Call;

/**
 * Retrieves Comics information given a  {@link ComicsQuery} or some simple params like the
 * character id. A valid {@link MarvelApiConfig} is needed.
 */
public final class ComicApiClient extends MarvelApiClient {

    public ComicApiClient(MarvelApiConfig marvelApiConfig) {
        super(marvelApiConfig);
    }

    public MarvelResponse<ComicsDto> getAll(int offset, int limit) throws MarvelApiException {
        ComicsQuery query = ComicsQuery.Builder.create().withOffset(offset).withLimit(limit).build();
        return getAll(query);
    }

    public MarvelResponse<ComicsDto> getAll(ComicsQuery comicsQuery) throws MarvelApiException {
        ComicApiRest api = getApi(ComicApiRest.class);

        Map<String, Object> queryAsMap = comicsQuery.toMap();
        Call<MarvelResponse<ComicsDto>> call = api.getComics(queryAsMap);
        return execute(call);
    }

    public MarvelResponse<ComicDto> getComic(String comicId) throws MarvelApiException {
        if (comicId == null || comicId.isEmpty()) {
            throw new IllegalArgumentException("The ComicId must not be null or empty");
        }
        ComicApiRest api = getApi(ComicApiRest.class);

        Call<MarvelResponse<ComicsDto>> call = api.getComic(comicId);
        MarvelResponse<ComicsDto> comics = execute(call);
        ComicsDto comicsDto = comics.getResponse();
        if (comicsDto != null && comicsDto.getCount() > 0) {
            ComicDto comicDto = comicsDto.getComics().get(0);
            MarvelResponse<ComicDto> comicResponse = new MarvelResponse<>(comics);
            comicResponse.setResponse(comicDto);
            return comicResponse;
        } else {
            throw new MarvelApiException("Comic not found", null);
        }
    }
}