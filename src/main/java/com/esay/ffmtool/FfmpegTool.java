package com.esay.ffmtool;

import android.app.Activity;
import android.util.Log;

/**
 * Created by ZBK on 2017/9/28.
 * Describe:
 */

public class FfmpegTool {
    /**
     * 裁剪视频的回调接口
     */
    public interface VideoResult{
        /**
         * 裁剪结果回调
         * @param code 返回码
         * @param src 视频源
         * @param dst 裁剪结果保存地址
         * @param sucess 裁剪是否成功 true 成功
         */
        public void clipResult(int code,String src,String dst,boolean sucess,int tag);
    }

    public interface ImageDecodeing{
        public void sucessOne(String path,int i);
    }


    private static FfmpegTool instance;

    private Activity activity;
    private ImageDecodeing imageDecodeing;


    public ImageDecodeing getImageDecodeing() {
        return imageDecodeing;
    }

    public void setImageDecodeing(ImageDecodeing imageDecodeing) {
        this.imageDecodeing = imageDecodeing;
    }

    private  FfmpegTool(){

    }

    private void init(Activity activity){
        this.activity=activity;
    }



    public static FfmpegTool getInstance(Activity activity){
        if (instance==null){
            synchronized(FfmpegTool.class){
                if (instance == null)instance = new FfmpegTool();
            }
        }
        instance.init(activity);
        return instance;
    }

    static {
        System.loadLibrary("avutil");
        System.loadLibrary("fdk-aac");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("swresample");
        System.loadLibrary("avfilter");
        System.loadLibrary("jxffmpegrun");
    }


    public static native int cmdRun(String[] cmd);

    public static native int decodToImage(String srcPath,String savePath,int startTime,int count);



    public int videoToImage(final String src, final String dir, int startTime, int count, final VideoResult call, final int tag){
        final int result=decodToImage(src,dir,startTime,count);
        if (call!=null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean ret=true;
                    if (result!=0){
                        ret=false;
                    }
                    call.clipResult(result,src,dir,ret,tag);
                }
            });
        }
        return result;
    }


    //ffmpeg -y -ss 10 -t 15 -i /storage/emulated/0/test/c.mp4 -vcodec copy -acodec copy -strict -2 /storage/emulated/0/test/out.mp4

    /**
     * 裁剪视频
     * @param src 视频源地址
     * @param dst 裁剪结果
     * @param startTime 开始裁剪时间
     * @param duration 裁剪时长
     * @return
     */
    public  int clipVideo(final String src, final String dst, int startTime, int duration, final int tag, final VideoResult call){
        String cmd=String.format("ffmpeg -y -ss "+startTime+" -t "+duration+
        " -i "+src+" -vcodec copy -acodec copy -strict -2 "+dst);
        String regulation="[ \\t]+";
        Log.i("clipVideo","cmd:"+cmd);
        final String[] split = cmd.split(regulation);
        final int result=cmdRun(split);
        if (call!=null){
           activity.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   call.clipResult(result,src,dst,result==0,tag);
               }
           });
        }
        return result;
    }

//ffmpeg -y -i /storage/emulated/0/esay/temp/temp1506592501.mp4 -b:v 1500k -bufsize 1500k -maxrate 2000k -g 26 /storage/emulated/0/esay/temp/temp1506592521.mp4

    /**
     * 压缩视频
     * @param src 视频源地址
     * @param dst 结果目录
     * @param tag 标志位
     * @param call 回调
     * @return
     */
    public int compressVideo(final String src,final String dst, final int tag, final VideoResult call){
        final String dst2=dst+"temp"+System.currentTimeMillis()/1000+".mp4";
        String cmd=String.format("ffmpeg -threads 32 -y -i "+src
                +" -b:v 1500k -bufsize 3000k -maxrate 2000k -preset superfast "+dst2);
        //cmd="ffmpeg -threads 64 -i /storage/emulated/0/test/out.mp4 -c:v libx264  -x264opts bitrate=2000:vbv-maxrate=2500  -crf 20 -preset superfast  -vbr 4   /storage/emulated/0/test/tes.mp4";
        String regulation="[ \\t]+";
        Log.i("compressVideo","cmd:"+cmd);
        final String[] split = cmd.split(regulation);
        final int result=cmdRun(split);
        if (call!=null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    call.clipResult(result,src,dst2,result==0,tag);
                }
            });
        }
        Log.i("compressVideo","result:"+result);
        return result;
    }

    /**
     * 解析图片过程中jni的回调方法
     * @param path
     * @param index
     */
    public void decodToImageCall(final  String path,final int index){
        if(imageDecodeing!=null){
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageDecodeing.sucessOne(path,index);
                }
            });
        }
        //Log.i("decodToImageCall","path:"+path+"___index:"+index);
    }

    public native int decodToImageWithCall(String srcPath,String savePath,int startTime,int count);


    public void relase(){
        this.activity=null;
    }


}
