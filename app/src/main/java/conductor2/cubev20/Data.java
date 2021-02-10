package conductor2.cubev20;

import android.app.Application;

public class Data extends Application {

    // 全局变量 图片张数
    private int NUM_PICTURES;
    public int getNUM_PICTURES(){
        return this.NUM_PICTURES;
    }
    public void setNUM_PICTURES(int new_num_pictures){
        this.NUM_PICTURES = new_num_pictures;
    }
    public void delNUM_PICTURES(){
        this.NUM_PICTURES = 0;
    }
    //全局变量 位图颜色数组
    private int PIC[][][][];
    public int[][][][] getPIC() {
        return this.PIC;
    }
    public void setPIC(int num_pictures, int x, int y, int z, int color_value) {
        this.PIC[num_pictures][x][2-y][z] = color_value;
    }
    public void delPIC(){
        this.PIC = new int[6][3][3][3];
    }

    @Override
    public void onCreate(){
        NUM_PICTURES = 0;
        PIC = new int[6][3][3][3];
        super.onCreate();
    }

}
