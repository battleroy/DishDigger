package by.bsu.battleroy.pmvs_lab03.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.widget.ImageView;

import by.bsu.battleroy.pmvs_lab03.R;

public class RecipeView extends ImageView {

    private String recipeId;
    private Paint footerBgPaint;
    private TextPaint footerTextPaint;
    private Rect footerRect;

    private Context context;

    private String recipeName;

    public RecipeView(Context context, String recipeName, String recipeId) {
        super(context);
        this.context = context;
        this.recipeName = recipeName;
        this.recipeId = recipeId;

        footerBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        footerBgPaint.setColor(ContextCompat.getColor(context, R.color.colorFooterBg));
        footerBgPaint.setAlpha(120);

        footerTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        footerTextPaint.setColor(ContextCompat.getColor(context, R.color.colorFooterText));
        footerTextPaint.setAlpha(255);
        footerTextPaint.setTextSize(20);
        footerTextPaint.setTextAlign(Paint.Align.LEFT);

        footerRect = new Rect();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        footerRect.set(0, canvas.getHeight() * 3 / 4, canvas.getWidth(), canvas.getHeight());

        canvas.drawRect(footerRect, footerBgPaint);

        StaticLayout sl = new StaticLayout(
                recipeName,
                footerTextPaint,
                footerRect.width(),
                Layout.Alignment.ALIGN_NORMAL,
                5, 5, true);

        canvas.save();

        float textXCoordinate = footerRect.left + 5;
        float textYCoordinate = footerRect.top + 5;

        canvas.translate(textXCoordinate, textYCoordinate);

        sl.draw(canvas);
        canvas.restore();
    }

    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }
}
