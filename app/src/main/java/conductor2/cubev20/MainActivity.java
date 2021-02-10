package conductor2.cubev20;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 扫描按钮设置
        Button button_take;
        button_take = findViewById(R.id.button_take);
        button_take.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                final Data app = (Data)getApplication();
                int num_pictures = app.getNUM_PICTURES();
                if (num_pictures < 6) {
                    Intent intent = new Intent(MainActivity.this, Takephoto.class);
                    startActivity(intent);
                }else {
                    AlertDialog.Builder builder_take = new AlertDialog.Builder(MainActivity.this);
                    builder_take.setTitle("错误");
                    builder_take.setIcon(getResources().getDrawable(R.drawable.image_logo_1));
                    builder_take.setMessage("已采集够六张图片！");
                    builder_take.setNeutralButton("确定",null);
                    builder_take.show();
                }
            }
        });
        // 清除所有图片按钮设置
        Button button_delete;
        button_delete = findViewById(R.id.button_delete);
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder_delete = new AlertDialog.Builder(MainActivity.this);
                builder_delete.setTitle("警告");
                builder_delete.setIcon(getResources().getDrawable(R.drawable.image_logo_1));
                builder_delete.setMessage("是否删除所有已采集图片？");
                builder_delete.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear_pictures();
                        final Data app = (Data)getApplication();
                        app.delNUM_PICTURES();
                        app.delPIC();
                        dialog.dismiss();
                    }
                });
                builder_delete.setNegativeButton("取消",null);
                builder_delete.show();
            }
        });
        // 开始自动还原按钮设置
        Button button_run;
        button_run = findViewById(R.id.button_run);
        button_run.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Data app = (Data)getApplication();
                int num_pictures = app.getNUM_PICTURES();
                if (num_pictures == 6){
                    Intent intent = new Intent(MainActivity.this, Run.class);
                    startActivity(intent);
                }else {
                    AlertDialog.Builder builder_run = new AlertDialog.Builder(MainActivity.this);
                    builder_run.setTitle("错误");
                    builder_run.setIcon(getResources().getDrawable(R.drawable.image_logo_1));
                    builder_run.setMessage("未采集够六张图片！");
                    builder_run.setNeutralButton("确定",null);
                    builder_run.show();
                }
            }
        });
        // 程序退出按钮设置
        Button button_exit;
        button_exit = findViewById(R.id.button_exit);
        button_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(0);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clear_pictures();
        display_pictures();
    }

    // 依次显示六张位图
    public void display_pictures(){
        final Data app = (Data) getApplication();
        int num_pictures = app.getNUM_PICTURES();
        int[][][][] pic = app.getPIC();
        int[] view_index = {R.id.imageView_0, R.id.imageView_1, R.id.imageView_2, R.id.imageView_3, R.id.imageView_4, R.id.imageView_5};
        for (int i = 0; i < num_pictures; i++) {
            // 建立一个小图布进行画图
            Bitmap current_bmp = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(current_bmp);
            canvas.drawColor(Color.GRAY);
            Paint p = new Paint();
            p.setStyle(Paint.Style.FILL);
            for (int x = 0; x <= 2; x++){
                for (int y = 0; y <= 2; y++){
                    int[] current_color = {pic[i][x][y][0], pic[i][x][y][1], pic[i][x][y][2]};
                    p.setColor(Color.rgb(current_color[0], current_color[1], current_color[2]));
                    canvas.drawRect(20 + 60 * x, 20 + 60 * (2 - y), 60 + 60 * x, 60 + 60 * (2 - y), p);
                }
            }
            // 将这个小图传到对应的ImageView中
            Drawable current_drawable = new BitmapDrawable(current_bmp);
            ImageView current_view = findViewById(view_index[i]);
            current_view.setImageDrawable(current_drawable);
        }
    }

    public void clear_pictures(){
        final Data app = (Data) getApplication();
        int num_pictures = app.getNUM_PICTURES();
        int[] view_index = {R.id.imageView_0, R.id.imageView_1, R.id.imageView_2, R.id.imageView_3, R.id.imageView_4, R.id.imageView_5};
        for (int i = 0; i < num_pictures; i++) {
            ImageView current_view = findViewById(view_index[i]);
            current_view.setImageDrawable(null);
        }
    }


}
