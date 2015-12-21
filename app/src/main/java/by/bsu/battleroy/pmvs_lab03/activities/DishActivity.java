package by.bsu.battleroy.pmvs_lab03.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import by.bsu.battleroy.pmvs_lab03.R;
import by.bsu.battleroy.pmvs_lab03.util.BitmapFiltering;

public class DishActivity extends AppCompatActivity {

    private ScrollView svDish;
    private TextView tvDishName;
    // private ImageView ivDishImage;
    private TextView tvPublisherName;
    private TextView tvIngredientsBody;
    private RatingBar rbDish;
    private String dishId;

    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            dishId = extras.getString(MainActivity.RECIPE_ID_KEY);
        }

        svDish = (ScrollView) findViewById(R.id.sv_dish);
        tvDishName = (TextView) findViewById(R.id.tv_dish_name);
        // ivDishImage = (ImageView) findViewById(R.id.iv_dish);
        tvPublisherName = (TextView) findViewById(R.id.tv_publisher_name);
        tvIngredientsBody = (TextView) findViewById(R.id.tv_ingredients_body);
        rbDish = (RatingBar) findViewById(R.id.rb_dish);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.wait));

        new RecipeDetailsDownloadTask().execute(dishId);
    }

    private class RecipeDetailsDownloadTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            svDish.setVisibility(View.INVISIBLE);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            svDish.setVisibility(View.VISIBLE);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(String... params) {
            BufferedReader br = null;
            try {
                HttpParams httpParameters = new BasicHttpParams();
                int timeoutConnection = 30000;
                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                int timeoutSocket = 6000;
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
                HttpClient client = new DefaultHttpClient(httpParameters);

                URI website = new URI("http://food2fork.com/api/get?key=5fd809d60860de26c90d0a949ac3b781&rId=" + params[0]);
                HttpGet request = new HttpGet(website);
                HttpResponse response = client.execute(request);
                br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                String jsonData = getJSONFromBufferedReader(br);
                fillRecipeDataFromJSON(jsonData);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            } catch (ClientProtocolException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DishActivity.this, "Check your internet", Toast.LENGTH_SHORT).show();
                    }
                });
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        private String getJSONFromBufferedReader(BufferedReader br) throws IOException {
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }

        private void fillRecipeDataFromJSON(String jsonData) throws JSONException, IOException {
            final JSONObject jsonRecipe = new JSONObject(jsonData).getJSONObject("recipe");
            final URL url = new URL(jsonRecipe.getString("image_url"));
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            bitmap = BitmapFiltering.fastBlur(bitmap, 1f, 5);
            bitmap = BitmapFiltering.brightness(bitmap, 60);
            int dstSize = Math.max(svDish.getWidth(), svDish.getHeight());
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dstSize, dstSize);
            final Bitmap blurred = BitmapFiltering.scaleCenterCrop(bitmap, svDish.getWidth(), svDish.getHeight());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // ivDishImage.setImageBitmap(bitmap);
                        tvPublisherName.setText(Html.fromHtml(jsonRecipe.getString("publisher")));
                        tvDishName.setText(Html.fromHtml(jsonRecipe.getString("title")));
                        tvIngredientsBody.setText(getIngredientsFromJSONArray(jsonRecipe.getJSONArray("ingredients")));
                        Double rank = jsonRecipe.getDouble("social_rank");
                        rbDish.setRating((float) (rank / 100 * rbDish.getMax()));
                        svDish.setBackground(new BitmapDrawable(getResources(), blurred));
                        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            Palette palette = Palette.generate(bitmap);
                            int primaryDark = getResources().getColor(R.color.colorPrimaryDark, getTheme());
                            int vibrantColor = palette.getLightMutedColor(primaryDark);
                            getWindow().setStatusBarColor(vibrantColor);
                        }*/
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        private String getIngredientsFromJSONArray(JSONArray ingredients) throws JSONException {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < ingredients.length(); ++i) {
                sb.append(i + 1).append(". ").append(Html.fromHtml(ingredients.get(i).toString())).append("\n");
            }
            return sb.toString();
        }
    }
}
