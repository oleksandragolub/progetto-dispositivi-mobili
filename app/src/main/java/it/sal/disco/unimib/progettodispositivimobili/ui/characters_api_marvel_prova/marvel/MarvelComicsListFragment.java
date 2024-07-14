package it.sal.disco.unimib.progettodispositivimobili.ui.characters_api_marvel_prova.marvel;

/*public class MarvelComicsListFragment extends Fragment {
    private static final String PUBLIC_KEY = "93e5146b36c6609ec6a87d8104728ed2";
    private static final String PRIVATE_KEY = "80e7b32472204a8f30779ecb3e20815e84384d7b";
    private static final String TAG = "MarvelComicsListFragment";
    private RecyclerView recyclerView;
    private MarvelComicsAdapter comicsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marvel_comics_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        comicsAdapter = new MarvelComicsAdapter();
        recyclerView.setAdapter(comicsAdapter);
        fetchComics("");
        return view;
    }

    private void fetchComics(String genre) {
        long ts = System.currentTimeMillis();
        String hash = getMd5(ts + PRIVATE_KEY + PUBLIC_KEY);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ComicResponse> call = apiService.getComics(ts, PUBLIC_KEY, hash, "comic", "comic", true, "thisMonth", genre, 100);
        call.enqueue(new Callback<ComicResponse>() {
            @Override
            public void onResponse(Call<ComicResponse> call, Response<ComicResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Comix> comixes = response.body().getData().getResults();
                    if (comixes.isEmpty()) {
                        Toast.makeText(getContext(), "No comixes found", Toast.LENGTH_SHORT).show();
                    } else {
                        comicsAdapter.setComics(comixes);
                    }
                } else {
                    Toast.makeText(getContext(), "Request not successful", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ComicResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Request failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getMd5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}*/