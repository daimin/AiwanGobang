package com.codecos.gobang;


/**
 * Created by Administrator on 2016/9/10.
 */
public class Player {

    String name;
    int score;
    String title;
    String dp;
    int [] rgbColor;

    private int levelColor [][] = new int [][]{
            {0x00, 0x00, 0x00}, //无 100分以上
            {0x99, 0xCC, 0x99}, //青铜 100-2000
            {0xC0, 0xC0, 0xC0}, //白银 2000-4000
            {0xFF, 0xFF, 0x00}, //黄金 4000-8000
            {0xFF, 0xFF, 0xCC}, //白金 8000-15000
            {0x99, 0x33, 0xFF}, //大师 15000-30000
            {0xFF, 0x99, 0x00}, //宗师 30000以上
    };

    public Player(String name, String dp, int score) {
        this.name = name;
        this.score = score;
        this.dp = dp;
        this.rgbColor = this.getLevelColor();

    }



    public int [] getLevelColor(){
        if(score < 100){
            this.title = "无";
            return this.levelColor[0];
        }else if(score >= 100 && score < 2000){
            this.title = "青铜";
            return this.levelColor[1];
        }else if(score >= 2000 && score < 4000){
            this.title = "白银";
            return this.levelColor[2];
        }else if(score >= 4000 && score < 8000){
            this.title = "黄金";
            return this.levelColor[3];
        }else if(score >= 8000 && score < 15000){
            this.title = "白金";
            return this.levelColor[4];
        }else if(score >= 15000 && score < 30000){
            this.title = "大师";
            return this.levelColor[5];
        }else{
            this.title = "宗师";
            return this.levelColor[6];
        }
    }



}
