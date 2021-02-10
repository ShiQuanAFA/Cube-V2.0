package conductor2.cubev20;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

public class Run extends AppCompatActivity {

    private final int max_step = 6;
    private final int[] target = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54};
    private int ok = 0;
    private int[] result;
    private int[][] index_color = new int[6][3];
    private long progress_count = 0;
    private long total_progress_count = (long) (pow(18, max_step) + pow(18, max_step - 1) + 2 * pow(18, max_step - 2));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开启后台异步线程
        final RunThread runThread = new RunThread();
        runThread.start();
        // 设置定时更新进度
        Timer timer = new Timer();
        final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                updateProgress();
                if (ok == 1){
                    // 发送给前台结果显示监听器
                    Message message = handler.obtainMessage();
                    message.arg1 = 1;
                    handler.sendMessage(message);
                    // 停止后台线程
                    runThread.interrupt();
                    this.cancel();
                }
            }
        };
        // 未出结果时隔两秒一更新
        timer.schedule(task, 2000, 2000);
    }

    // 定义前台监听器
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 出结果后立即显示
            display();
        }
    };

    // 定义后台异步线程
    public class RunThread extends Thread{
        @Override
        public void run(){
            super.run();
            Log.i("thread","RunThread");
            // 六张图片颜色矩阵转立方序号矩阵
            int[][][] cube = getCube();
            // 立方序号矩阵转一维位置数组
            int[] array = getArray(cube);
            // 开始进行魔方还原步骤计算并保存结果
            getResult(array, max_step);
        }
    }

    // 六张图片颜色矩阵转立方序号矩阵
    public int[][][] getCube(){
        // 获取已储存的六张图片颜色矩阵
        final Data app = (Data)getApplication();
        int[][][][] pic;
        pic = app.getPIC();
        // 标定六种颜色的序号
        for (int i = 0; i <= 5; i++){
            index_color[i][0] = pic[i][1][1][0];
            index_color[i][1] = pic[i][1][1][1];
            index_color[i][2] = pic[i][1][1][2];
        }
        // k邻近算法
        int[][][] cube = new int[6][3][3];
        for (int i = 0; i <= 5; i++){
            for (int x = 0; x <= 2; x++){
                for (int y = 0; y <= 2; y++){
                    // 遍历搜寻最短欧式距离的对应颜色序号
                    int min_loss = (int) pow(2,20);
                    int best_k = 0;
                    for (int k = 0; k <= 5; k++){
                        int loss = abs(pic[i][x][y][0] - index_color[k][0]) + abs(pic[i][x][y][1] - index_color[k][1]) + abs(pic[i][x][y][2] - index_color[k][2]);
                        if (loss < min_loss){
                            min_loss = loss;
                            best_k = k;
                        }
                    }
                    // 得到最佳匹配序号
                    cube[i][x][y] = best_k;
                }
            }
        }
        return cube;
    }

    // 立方序号矩阵转一维位置数组
    @SuppressWarnings("ConstantConditions")
    public int[] getArray(int[][][] cube){
        int[] array = new int[55];
        for (int i = 1; i <= 54; i++){
            int[] current_side = stringToArray(posSide.get(i));
            if (current_side.length == 9){
                String current_key = cube[current_side[0]][current_side[1]][current_side[2]] + ", "
                        + cube[current_side[3]][current_side[4]][current_side[5]] + ", " + cube[current_side[6]][current_side[7]][current_side[8]];
                array[i] = sideValue.get(current_key);
            }else if (current_side.length == 6){
                String current_key = cube[current_side[0]][current_side[1]][current_side[2]] + ", "
                        + cube[current_side[3]][current_side[4]][current_side[5]];
                array[i] = sideValue.get(current_key);
            }else {
                array[i] = i;
            }
        }
        return array;
    }

    // 立方转数组模块计算常量：
    // 位置相邻色面字典
    public HashMap<Integer, String> posSide = new HashMap<Integer, String>(){
        {
            put(1, "0, 0, 0, 4, 2, 2, 3, 0, 2");
            put(2, "0, 1, 0, 3, 1, 2");
            put(3, "0, 2, 0, 3, 2, 2, 5, 0, 2");
            put(4, "0, 0, 1, 4, 1, 2");
            put(5, "5");
            put(6, "0, 2, 1, 5, 1, 2");
            put(7, "0, 0, 2, 4, 0, 2, 1, 0, 0");
            put(8, "0, 1, 2, 1, 1, 0");
            put(9, "0, 2, 2, 1, 2, 0, 5, 2, 2");
            put(10, "1, 0, 0, 0, 0, 2, 4, 0, 2");
            put(11, "1, 1, 0, 0, 1, 2");
            put(12, "1, 2, 0, 0, 2, 2, 5, 2, 2");
            put(13, "1, 0, 1, 4, 0, 1");
            put(14, "14");
            put(15, "1, 2, 1, 5, 2, 1");
            put(16, "1, 0, 2, 4, 0, 0, 2, 0, 0");
            put(17, "1, 1, 2, 2, 1, 0");
            put(18, "1, 2, 2, 2, 2, 0, 5, 2, 0");
            put(19, "2, 0, 0, 1, 0, 2, 4, 0, 0");
            put(20, "2, 1, 0, 1, 1, 2");
            put(21, "2, 2, 0, 1, 2, 2, 5, 2, 0");
            put(22, "2, 0, 1, 4, 1, 0");
            put(23, "23");
            put(24, "2, 2, 1, 5, 1, 0");
            put(25, "2, 0, 2, 4, 2, 0, 3, 0, 0");
            put(26, "2, 1, 2, 3, 1, 0");
            put(27, "2, 2, 2, 3, 2, 0, 5, 0, 0");
            put(28, "3, 0, 0, 2, 0, 2, 4, 2, 0");
            put(29, "3, 1, 0, 2, 1, 2");
            put(30, "3, 2, 0, 2, 2, 2, 5, 0, 0");
            put(31, "3, 0, 1, 4, 2, 1");
            put(32, "32");
            put(33, "3, 2, 1, 5, 0, 1");
            put(34, "3, 0, 2, 0, 0, 0, 4, 2, 2");
            put(35, "3, 1, 2, 0, 1, 0");
            put(36, "3, 2, 2, 0, 2, 0, 5, 0, 2");
            put(37, "4, 0, 0, 2, 0, 0, 1, 0, 2");
            put(38, "4, 1, 0, 2, 0, 1");
            put(39, "4, 2, 0, 2, 0, 2, 3, 0, 0");
            put(40, "4, 0, 1, 1, 0, 1");
            put(41, "41");
            put(42, "4, 2, 1, 3, 0, 1");
            put(43, "4, 0, 2, 0, 0, 2, 1, 0, 0");
            put(44, "4, 1, 2, 0, 0, 1");
            put(45, "4, 2, 2, 0, 0, 0, 3, 0, 2");
            put(46, "5, 0, 0, 3, 2, 0, 2, 2, 2");
            put(47, "5, 1, 0, 2, 2, 1");
            put(48, "5, 2, 0, 2, 2, 0, 1, 2, 2");
            put(49, "5, 0, 1, 3, 2, 1");
            put(50, "50");
            put(51, "5, 2, 1, 1, 2, 1");
            put(52, "5, 0, 2, 0, 2, 0, 3, 2, 2");
            put(53, "5, 1, 2, 0, 2, 1");
            put(54, "5, 2, 2, 0, 2, 2, 1, 2, 0");
        }
    };

    public int[] stringToArray(String string){
        String[] charArray = string.split(", ");
        int[] current_side = new int[charArray.length];
        for (int i = 0; i < current_side.length; i++){
            current_side[i] = Integer.valueOf(charArray[i]);
        }
        return current_side;
    }
    // 相邻色面序号字典
    public HashMap<String, Integer> sideValue = new HashMap<String, Integer>(){
        {
            put("0, 3, 4",1);
            put("0, 4, 3",1);
            put("0, 3, 5",3);
            put("0, 5, 3",3);
            put("0, 1, 4",7);
            put("0, 4, 1",7);
            put("0, 1, 5",9);
            put("0, 5, 1",9);
            put("1, 0, 4",10);
            put("1, 4, 0",10);
            put("1, 0, 5",12);
            put("1, 5, 0",12);
            put("1, 4, 2",16);
            put("1, 2, 4",16);
            put("1, 2, 5",18);
            put("1, 5, 2",18);
            put("2, 1, 4",19);
            put("2, 4, 1",19);
            put("2, 1, 5",21);
            put("2, 5, 1",21);
            put("2, 3, 4",25);
            put("2, 4, 3",25);
            put("2, 3, 5",27);
            put("2, 5, 3",27);
            put("3, 2, 4",28);
            put("3, 4, 2",28);
            put("3, 2, 5",30);
            put("3, 5, 2",30);
            put("3, 0, 4",34);
            put("3, 4, 0",34);
            put("3, 0, 5",36);
            put("3, 5, 0",36);
            put("4, 1, 2",37);
            put("4, 2, 1",37);
            put("4, 2, 3",39);
            put("4, 3, 2",39);
            put("4, 0, 1",43);
            put("4, 1, 0",43);
            put("4, 0, 3",45);
            put("4, 3, 0",45);
            put("5, 2, 3",46);
            put("5, 3, 2",46);
            put("5, 1, 2",48);
            put("5, 2, 1",48);
            put("5, 0, 3",52);
            put("5, 3, 0",52);
            put("5, 0, 1",54);
            put("5, 1, 0",54);
            put("0, 3",2);
            put("0, 4",4);
            put("0, 5",6);
            put("0, 1",8);
            put("1, 0",11);
            put("1, 4",13);
            put("1, 5",15);
            put("1, 2",17);
            put("2, 1",20);
            put("2, 4",22);
            put("2, 5",24);
            put("2, 3",26);
            put("3, 2",29);
            put("3, 4",31);
            put("3, 5",33);
            put("3, 0",35);
            put("4, 2",38);
            put("4, 1",40);
            put("4, 3",42);
            put("4, 0",44);
            put("5, 2",47);
            put("5, 3",49);
            put("5, 1",51);
            put("5, 0",53);
        }
    };

    // 开始进行魔方还原步骤计算并保存结果
    public void getResult(int[] array, int max_step){
        for (int k = 1; k <= max_step; k++){
            ArrayList<Integer> step_list = new ArrayList<>();
            addStep(array, step_list, 0, k);
        }
    }

    @SuppressWarnings("unchecked")
    public void addStep(int[] array, ArrayList<Integer> step_list, int count, int max_count){
        if (count == max_count){
            judge(array, step_list, 0);
        }else {
            for (int i = 0; i <= 17; i++){
                ArrayList<Integer> step_list_next = (ArrayList<Integer>) step_list.clone();
                step_list_next.add(i);
                addStep(array, step_list_next, count + 1, max_count);
            }
        }
    }

    public void judge(int[] array, ArrayList<Integer> step_list, int count){
        if (count == step_list.size()){
            progress_count++;
            if (Arrays.equals(array, target)){
                // 保存结果
                result = new int[step_list.size()];
                for (int k = 0; k < step_list.size(); k++){
                    result[k] = step_list.get(k);
                }
                Log.d("RunThread", "Got result!");
                ok = 1;
            }
        }else {
            judge(operation(array, step_list.get(count)), step_list, count + 1);
        }
    }

    // 更新进度与显示结果
    public void display(){
        // 清除进度条与指示文字
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        TextView textView = findViewById(R.id.textView_result);
        textView.setVisibility(View.INVISIBLE);
        // 显示结果步骤图
        int num_step = result.length;
        int[] colorView_index = new int[]{R.id.colorView_0, R.id.colorView_1, R.id.colorView_2, R.id.colorView_3, R.id.colorView_4, R.id.colorView_5};
        int[] operationView_index = new int[]{R.id.operationView_0, R.id.operationView_1, R.id.operationView_2, R.id.operationView_3, R.id.operationView_4, R.id.operationView_5};
        int[] operationImg_index = new int[]{R.drawable.image_operation_0, R.drawable.image_operation_1, R.drawable.image_operation_2};
        for (int i = 0; i < num_step; i++){
            ImageView current_colorView = findViewById(colorView_index[i]);
            ImageView current_operationView = findViewById(operationView_index[i]);
            // 画对应色面图
            Bitmap current_bmp = Bitmap.createBitmap(current_colorView.getWidth(), current_colorView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(current_bmp);
            canvas.drawColor(Color.rgb(index_color[result[i]/3][0], index_color[result[i]/3][1], index_color[result[i]/3][2]));
            Drawable current_drawable = new BitmapDrawable(current_bmp);
            current_colorView.setImageDrawable(current_drawable);
            // 画对应操作图
            current_operationView.setImageResource(operationImg_index[result[i]%3]);
        }
    }

    @SuppressLint("SetTextI18n")
    public void updateProgress(){
        // 计算进度
        int progress = (int) (100 * progress_count / total_progress_count);
        // 更新进度条
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(progress);
        TextView textView = findViewById(R.id.textView_result);
        textView.setText("计算中\n"+progress+"%");
    }

    // 定义十八种操作
    public int[] operation(int[] array, int num_operation){
        int[] a = array.clone();
        int p, q, x, y, z, d, e, f;
        if (num_operation == 0){
            p=a[1];
            a[1]=a[7];
            a[7]=a[9];
            a[9]=a[3];
            a[3]=p;

            p=a[2];
            a[2]=a[4];
            a[4]=a[8];
            a[8]=a[6];
            a[6]=p;

            x=a[45];
            y=a[44];
            z=a[43];

            a[45]=a[10];
            a[44]=a[11];
            a[43]=a[12];

            a[10]=a[54];
            a[11]=a[53];
            a[12]=a[52];

            a[54]=a[36];
            a[53]=a[35];
            a[52]=a[34];

            a[36]=x;
            a[35]=y;
            a[34]=z;
        }
        if (num_operation == 1){
            p=a[1];
            a[1]=a[3];
            a[3]=a[9];
            a[9]=a[7];
            a[7]=p;

            p=a[2];
            a[2]=a[6];
            a[6]=a[8];
            a[8]=a[4];
            a[4]=p;

            x=a[45];
            y=a[44];
            z=a[43];

            a[45]=a[36];
            a[44]=a[35];
            a[43]=a[34];

            a[36]=a[54];
            a[35]=a[53];
            a[34]=a[52];

            a[54]=a[10];
            a[53]=a[11];
            a[52]=a[12];

            a[10]=x;
            a[11]=y;
            a[12]=z;
        }
        if (num_operation == 2){
            p=a[1];
            q=a[3];
            a[1]=a[9];
            a[3]=a[7];
            a[9]=p;
            a[7]=q;

            p=a[2];
            q=a[4];
            a[2]=a[8];
            a[4]=a[6];
            a[8]=p;
            a[6]=q;


            x=a[45];
            y=a[44];
            z=a[43];
            d=a[36];
            e=a[35];
            f=a[34];

            a[45]=a[54];
            a[44]=a[53];
            a[43]=a[52];

            a[36]=a[10];
            a[35]=a[11];
            a[34]=a[12];

            a[54]=x;
            a[53]=y;
            a[52]=z;

            a[10]=d;
            a[11]=e;
            a[12]=f;
        }
        if (num_operation == 3){
            p=a[10];
            a[10]=a[16];
            a[16]=a[18];
            a[18]=a[12];
            a[12]=p;

            p=a[11];
            a[11]=a[13];
            a[13]=a[17];
            a[17]=a[15];
            a[15]=p;

            x=a[43];
            y=a[40];
            z=a[37];

            a[43]=a[19];
            a[40]=a[20];
            a[37]=a[21];

            a[19]=a[48];
            a[20]=a[51];
            a[21]=a[54];

            a[48]=a[9];
            a[51]=a[8];
            a[54]=a[7];

            a[9]=x;
            a[8]=y;
            a[7]=z;
        }
        if (num_operation == 4){
            p=a[10];
            a[10]=a[12];
            a[12]=a[18];
            a[18]=a[16];
            a[16]=p;

            p=a[11];
            a[11]=a[15];
            a[15]=a[17];
            a[17]=a[13];
            a[13]=p;

            x=a[43];
            y=a[40];
            z=a[37];

            a[43]=a[9];
            a[40]=a[8];
            a[37]=a[7];

            a[9]=a[48];
            a[8]=a[51];
            a[7]=a[54];

            a[48]=a[19];
            a[51]=a[20];
            a[54]=a[21];

            a[19]=x;
            a[20]=y;
            a[21]=z;
        }
        if (num_operation == 5){
            p=a[10];
            q=a[12];
            a[10]=a[18];
            a[12]=a[16];
            a[18]=p;
            a[16]=q;

            p=a[11];
            q=a[13];
            a[11]=a[17];
            a[13]=a[15];
            a[17]=p;
            a[15]=q;


            x=a[7];
            y=a[8];
            z=a[9];
            d=a[37];
            e=a[40];
            f=a[43];

            a[7]=a[21];
            a[8]=a[20];
            a[9]=a[19];

            a[37]=a[54];
            a[40]=a[51];
            a[43]=a[48];

            a[21]=x;
            a[20]=y;
            a[19]=z;

            a[54]=d;
            a[51]=e;
            a[48]=f;
        }
        if (num_operation == 6){
            p=a[19];
            a[19]=a[25];
            a[25]=a[27];
            a[27]=a[21];
            a[21]=p;

            p=a[20];
            a[20]=a[22];
            a[22]=a[26];
            a[26]=a[24];
            a[24]=p;

            x=a[37];
            y=a[38];
            z=a[39];

            a[37]=a[28];
            a[38]=a[29];
            a[39]=a[30];

            a[28]=a[46];
            a[29]=a[47];
            a[30]=a[48];

            a[46]=a[18];
            a[47]=a[17];
            a[48]=a[16];

            a[18]=x;
            a[17]=y;
            a[16]=z;
        }
        if (num_operation == 7){
            p=a[19];
            a[19]=a[21];
            a[21]=a[27];
            a[27]=a[25];
            a[25]=p;

            p=a[20];
            a[20]=a[24];
            a[24]=a[26];
            a[26]=a[22];
            a[22]=p;

            x=a[37];
            y=a[38];
            z=a[39];

            a[37]=a[18];
            a[38]=a[17];
            a[39]=a[16];

            a[18]=a[46];
            a[17]=a[47];
            a[16]=a[48];

            a[46]=a[28];
            a[47]=a[29];
            a[48]=a[30];

            a[28]=x;
            a[29]=y;
            a[30]=z;
        }
        if (num_operation == 8){
            p=a[19];
            q=a[21];
            a[19]=a[27];
            a[21]=a[25];
            a[27]=p;
            a[25]=q;

            p=a[20];
            q=a[22];
            a[20]=a[26];
            a[22]=a[24];
            a[26]=p;
            a[24]=q;


            x=a[16];
            y=a[17];
            z=a[18];
            d=a[39];
            e=a[38];
            f=a[37];

            a[16]=a[30];
            a[17]=a[29];
            a[18]=a[28];

            a[39]=a[48];
            a[38]=a[47];
            a[37]=a[46];

            a[30]=x;
            a[29]=y;
            a[28]=z;

            a[48]=d;
            a[47]=e;
            a[46]=f;
        }
        if (num_operation == 9){
            p=a[28];
            a[28]=a[34];
            a[34]=a[36];
            a[36]=a[30];
            a[30]=p;

            p=a[29];
            a[29]=a[31];
            a[31]=a[35];
            a[35]=a[33];
            a[33]=p;

            x=a[39];
            y=a[42];
            z=a[45];

            a[39]=a[1];
            a[42]=a[2];
            a[45]=a[3];

            a[1]=a[52];
            a[2]=a[49];
            a[3]=a[46];

            a[52]=a[27];
            a[49]=a[26];
            a[46]=a[25];

            a[27]=x;
            a[26]=y;
            a[25]=z;
        }
        if (num_operation == 10){
            p=a[28];
            a[28]=a[30];
            a[30]=a[36];
            a[36]=a[34];
            a[34]=p;

            p=a[29];
            a[29]=a[33];
            a[33]=a[35];
            a[35]=a[31];
            a[31]=p;

            x=a[39];
            y=a[42];
            z=a[45];

            a[39]=a[27];
            a[42]=a[26];
            a[45]=a[25];

            a[27]=a[52];
            a[26]=a[49];
            a[25]=a[46];

            a[52]=a[1];
            a[49]=a[2];
            a[46]=a[3];

            a[1]=x;
            a[2]=y;
            a[3]=z;
        }
        if (num_operation == 11){
            p=a[28];
            q=a[30];
            a[28]=a[36];
            a[30]=a[34];
            a[36]=p;
            a[34]=q;

            p=a[29];
            q=a[31];
            a[29]=a[35];
            a[31]=a[33];
            a[35]=p;
            a[33]=q;


            x=a[1];
            y=a[2];
            z=a[3];
            d=a[39];
            e=a[42];
            f=a[45];

            a[1]=a[27];
            a[2]=a[26];
            a[3]=a[25];

            a[39]=a[52];
            a[42]=a[49];
            a[45]=a[46];

            a[27]=x;
            a[26]=y;
            a[25]=z;

            a[52]=d;
            a[49]=e;
            a[46]=f;
        }
        if (num_operation == 12){
            p=a[39];
            a[39]=a[37];
            a[37]=a[43];
            a[43]=a[45];
            a[45]=p;

            p=a[42];
            a[42]=a[38];
            a[38]=a[40];
            a[40]=a[44];
            a[44]=p;

            x=a[34];
            y=a[31];
            z=a[28];

            a[34]=a[25];
            a[31]=a[22];
            a[28]=a[19];

            a[25]=a[16];
            a[22]=a[13];
            a[19]=a[10];

            a[16]=a[7];
            a[13]=a[4];
            a[10]=a[1];

            a[7]=x;
            a[4]=y;
            a[1]=z;
        }
        if (num_operation == 13){
            p=a[39];
            a[39]=a[45];
            a[45]=a[43];
            a[43]=a[37];
            a[37]=p;

            p=a[42];
            a[42]=a[44];
            a[44]=a[40];
            a[40]=a[38];
            a[38]=p;

            x=a[34];
            y=a[31];
            z=a[28];

            a[34]=a[7];
            a[31]=a[4];
            a[28]=a[1];

            a[7]=a[16];
            a[4]=a[13];
            a[1]=a[10];

            a[16]=a[25];
            a[13]=a[22];
            a[10]=a[19];

            a[25]=x;
            a[22]=y;
            a[19]=z;
        }
        if (num_operation == 14){
            p=a[39];
            q=a[45];
            a[39]=a[43];
            a[45]=a[37];
            a[43]=p;
            a[37]=q;

            p=a[42];
            q=a[38];
            a[42]=a[40];
            a[38]=a[44];
            a[40]=p;
            a[44]=q;


            x=a[16];
            y=a[13];
            z=a[10];
            d=a[19];
            e=a[22];
            f=a[25];

            a[16]=a[34];
            a[13]=a[31];
            a[10]=a[28];

            a[19]=a[1];
            a[22]=a[4];
            a[25]=a[7];

            a[34]=x;
            a[31]=y;
            a[28]=z;

            a[1]=d;
            a[4]=e;
            a[7]=f;
        }
        if (num_operation == 15){
            p=a[52];
            a[52]=a[54];
            a[54]=a[48];
            a[48]=a[46];
            a[46]=p;

            p=a[49];
            a[49]=a[53];
            a[53]=a[51];
            a[51]=a[47];
            a[47]=p;

            x=a[36];
            y=a[33];
            z=a[30];

            a[36]=a[9];
            a[33]=a[6];
            a[30]=a[3];

            a[9]=a[18];
            a[6]=a[15];
            a[3]=a[12];

            a[18]=a[27];
            a[15]=a[24];
            a[12]=a[21];

            a[27]=x;
            a[24]=y;
            a[21]=z;
        }
        if (num_operation == 16){
            p=a[52];
            a[52]=a[46];
            a[46]=a[48];
            a[48]=a[54];
            a[54]=p;

            p=a[49];
            a[49]=a[47];
            a[47]=a[51];
            a[51]=a[53];
            a[53]=p;

            x=a[36];
            y=a[33];
            z=a[30];

            a[36]=a[27];
            a[33]=a[24];
            a[30]=a[21];

            a[27]=a[18];
            a[24]=a[15];
            a[21]=a[12];

            a[18]=a[9];
            a[15]=a[6];
            a[12]=a[3];

            a[9]=x;
            a[6]=y;
            a[3]=z;
        }
        if (num_operation == 17){
            p=a[52];
            q=a[46];
            a[52]=a[48];
            a[46]=a[54];
            a[48]=p;
            a[54]=q;

            p=a[49];
            q=a[47];
            a[49]=a[51];
            a[47]=a[53];
            a[51]=p;
            a[53]=q;


            x=a[36];
            y=a[33];
            z=a[30];
            d=a[9];
            e=a[6];
            f=a[3];

            a[36]=a[18];
            a[33]=a[15];
            a[30]=a[12];

            a[9]=a[27];
            a[6]=a[24];
            a[3]=a[21];

            a[18]=x;
            a[15]=y;
            a[12]=z;

            a[27]=d;
            a[24]=e;
            a[21]=f;
        }
        return a;
    }


}
