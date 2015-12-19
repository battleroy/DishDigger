package by.bsu.battleroy.pmvs_lab03.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import by.bsu.battleroy.pmvs_lab03.R;
import by.bsu.battleroy.pmvs_lab03.model.Recipe;
import by.bsu.battleroy.pmvs_lab03.view.RecipeView;


public class MainActivity extends AppCompatActivity implements SearchBox.SearchListener, DialogInterface.OnCancelListener, View.OnClickListener {

    public static final String RECIPE_ID_KEY = "RECIPE_ID_KEY";

    private ProgressDialog progressDialog;

    private GridView gvRecipes;

    private List<Recipe> recipes;
    private List<Bitmap> recipeImages;

    private SearchBox sbDish;
    private Toolbar toolbar;
    private ImageView ivSearch;
    private TextView tvTapSearch;
    private RecipeDownloadTask currentDownloadTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recipes = new ArrayList<>();
        recipeImages = new ArrayList<>();

        gvRecipes = (GridView) findViewById(R.id.gv_recipes);
        gvRecipes.setAdapter(new RecipeViewAdapter(this));
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage(getString(R.string.wait));
        progressDialog.setOnCancelListener(this);

        ivSearch = (ImageView) findViewById(R.id.iv_search_icon);
        tvTapSearch = (TextView) findViewById(R.id.tv_tap_search);

        sbDish = (SearchBox) findViewById(R.id.sb_dish);
        sbDish.enableVoiceRecognition(this);
        sbDish.setLogoText("Search");
        sbDish.setLogoTextColor(ContextCompat.getColor(this, R.color.colorLogoText));
        sbDish.setSearchListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ivSearch.setVisibility(View.GONE);
                tvTapSearch.setVisibility(View.GONE);
                sbDish.revealFromMenuItem(R.id.action_search, MainActivity.this);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SearchBox.VOICE_RECOGNITION_CODE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {
                sbDish.populateEditText(matches.get(0));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSearchOpened() {

    }

    @Override
    public void onSearchCleared() {

    }

    @Override
    public void onSearchClosed() {
        sbDish.hideCircularly(this);
    }

    @Override
    public void onSearchTermChanged(String s) {

    }

    @Override
    public void onSearch(String s) {
        sbDish.hideCircularlyToMenuItem(R.id.action_search, this);
        toolbar.setTitle(s);
        currentDownloadTask = new RecipeDownloadTask();
        try {
            currentDownloadTask.execute(URLEncoder.encode(s, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResultClick(SearchResult searchResult) {

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (currentDownloadTask != null) {
            currentDownloadTask.cancel(true);
            currentDownloadTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        RecipeView recipeView = (RecipeView) v;
        Intent intent = new Intent(getBaseContext(), DishActivity.class);
        intent.putExtra(RECIPE_ID_KEY, recipeView.getRecipeId());
        startActivity(intent);
    }

    private class RecipeDownloadTask extends AsyncTask<String, Void, Void> {

        private HttpGet request;

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader br = null;
            try {
                HttpClient client = new DefaultHttpClient();
                URI website = new URI("http://food2fork.com/api/search?key=5fd809d60860de26c90d0a949ac3b781&q=" + params[0]);
                request = new HttpGet(website);
                HttpResponse response = client.execute(request);
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String jsonData = getJSONFromBufferedReader(br);
                recipes = getRecipesFromJSON(jsonData);
                recipeImages = getImagesForRecipes(recipes);
            } catch (Exception ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        gvRecipes.invalidateViews();
                    }
                });
                Log.e(ex.getClass().toString(), ex.getMessage());
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            currentDownloadTask = null;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    gvRecipes.invalidateViews();
                }
            });
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            recipes.clear();
            recipeImages.clear();
            gvRecipes.invalidateViews();
            request.abort();
        }

        private String getJSONFromBufferedReader(BufferedReader br) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private List<Recipe> getRecipesFromJSON(String jsonData) throws JSONException {
            List<Recipe> result = new ArrayList<>();
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray recipesArray = jsonObject.getJSONArray("recipes");
            for (int i = 0; i < recipesArray.length(); ++i) {
                JSONObject jsonRecipe = (JSONObject) recipesArray.get(i);
                Recipe recipe = new Recipe(
                        jsonRecipe.getString("recipe_id"),
                        jsonRecipe.getString("title"),
                        Uri.parse(jsonRecipe.getString("image_url"))
                );
                result.add(recipe);
            }
            return result;
        }

        private List<Bitmap> getImagesForRecipes(List<Recipe> recipes) throws IOException {
            List<Bitmap> result = new ArrayList<>();
            for (Recipe recipe : recipes) {
                URL url = new URL(recipe.getImageUri().toString());
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
                result.add(bitmap);
            }
            return result;
        }
    }

    private class RecipeViewAdapter extends BaseAdapter {

        private Context adapterContext;

        public RecipeViewAdapter(Context adapterContext) {
            this.adapterContext = adapterContext;
        }

        @Override
        public int getCount() {
            return recipes.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RecipeView recipeView = new RecipeView(adapterContext, recipes.get(position).getTitle(), recipes.get(position).getId());
            recipeView.setOnClickListener(MainActivity.this);
            recipeView.setLayoutParams(new GridView.LayoutParams(gvRecipes.getColumnWidth(), gvRecipes.getColumnWidth()));
            recipeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bitmap = recipeImages.get(position);
            if (bitmap != null) {
                recipeView.setImageBitmap(bitmap);
                return recipeView;
            }
            return null;
        }
    }
}
