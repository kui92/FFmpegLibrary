# FFmpegLibrary
    我们在实际开发过程中有时候会遇到这样的问题，有一个视频我们要拿到它的缩略图或者第几秒时间的画面。
    Android系统为我们提供了一个视频处理的类MediaMetadataRetriever，但是这个类一次只能获取一张图片，
    不能批量获取，使用循环太麻烦，而且对于分辨率大的视频时间太长。那么视频处理肯定要用到FFmpeg，使用FFmpeg命令行
    是非常方便的一种方式，对于一般的操作，视频的裁剪、压缩、转码等一条命令搞定。但是命令行在手机上进行某些操作的时候
    效率实在不怎么高，获取分辨率为1920*1080视频的一帧图形花费的时间为0.5秒左右，好处是可以批量获取，但是数量到了几十
    甚至上百的时候，就太漫长了。那么又想快又想批量处理的话就要用NDK直接用c语言对视频解码，找到图像帧转换成jpg保存到本地。
    
    
    该工程是一个library，有两种使用方式。
    1.直接下载下来，以module方式依赖在自己的项目中。
    2.gradle方式依赖。
    在项目根目录的gradle文件中添加
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
  
  再在app的gradle文件中添加依赖
  compile 'com.github.kui92:FFmpegLibrary:2.0'
  
  
  
      具体用法：在类FfmpegTool中有两个native方法
   //执行FFmpeg命令
   public static native int cmdRun(String[] cmd);
   
          使用方式：
   String cmd="FFmpeg命令";
   
   String regulation="[ \\t]+";
   
   
   final String[] split = cmd.split(regulation);
   
   
   final int result=FfmpegTool.cmdRun(split);
   
   
   
  
        //将视频解码，每一秒保存为一个jpg图片（因为关键帧的问题，在寻找时间时有时候可能有一两秒左右的偏差）
        //srcPath 视频地址  savePath 图片的保存路径 startTime 开始时间 count 要保存图片的数量一秒一幅
   public static native int decodToImage(String srcPath,String savePath,int startTime,int count);
    
    这两个方法都是阻塞的方法
    
    
    
    
    不想自己拼写命令的话还有几个封装方法，内部对命令进行了拼接，外部只需传递参数即可：
    
     /**
     * 视频裁剪
     * @param view
     */
    public void click1(View view){
       new Thread(){
           @Override
           public void run() {
               String basePath = Environment.getExternalStorageDirectory().getPath();
               String dir=basePath+File.separator+"test"+File.separator;
               String videoPath=dir+"c.mp4";
               String out=dir+"out.mp4";
               //参数说明 视频源  输出结果地址 开始时间单位s  视频时长单位s  标志位  回调
               ffmpegTool.clipVideo(videoPath, out, 10, 10, 1, new FfmpegTool.VideoResult() {
                   @Override
                   public void clipResult(int code, String src, String dst, boolean sucess, int tag) {
                       Log.i("MainActivity","code:"+code);
                       Log.i("MainActivity","src:"+src);
                       Log.i("MainActivity","dst:"+dst);
                       Log.i("MainActivity","sucess:"+sucess);
                       Log.i("MainActivity","tag:"+tag);
                       clipResutl=dst;
                   }
               });

           }
       }.start();
    }
    
    
    /**
     * 解码成图片
     * @param view
     */
    public void click2(View view){
        new  Thread(){
            @Override
            public void run() {
                String path= Environment.getExternalStorageDirectory().getPath()+ File.separator+"test"+File.separator;
                String video=path+"c.mp4";
                FfmpegTool.getInstance(MainActivity.this).videoToImage(video.replaceAll(File.separator, "/"), path.replaceAll(File.separator, "/"), 0, 60, new FfmpegTool.VideoResult() {
                    @Override
                    public void clipResult(int code, String src, String dst, boolean sucess, int tag) {
                        Log.i("MainActivity","code:"+code);
                        Log.i("MainActivity","src:"+src);
                        Log.i("MainActivity","dst:"+dst);
                        Log.i("MainActivity","sucess:"+sucess);
                        Log.i("MainActivity","tag:"+tag);
                    }
                },0);
            }
        }.start();

    }
    
    
    
    /**
     * 视频压缩
     */
    public void click3(View view){
        new Thread(){
            @Override
            public void run() {
                String path= Environment.getExternalStorageDirectory().getPath()+ File.separator+"test"+File.separator;
                //参数说明  视频源  压缩结果 标志位 回调
                FfmpegTool.getInstance(MainActivity.this).compressVideo("/storage/emulated/0/test/out.mp4", path, 2, new FfmpegTool.VideoResult() {
                    @Override
                    public void clipResult(int code, String src, String dst, boolean sucess, int tag) {
                        Log.i("MainActivity","code:"+code);
                        Log.i("MainActivity","src:"+src);
                        Log.i("MainActivity","dst:"+dst);
                        Log.i("MainActivity","sucess:"+sucess);
                        Log.i("MainActivity","tag:"+tag);
                    }
                });
            }
        }.start();
    }
    
    
    
    
 两个native方法的具体实现CSDN：http://blog.csdn.net/qq_28284547/article/details/78151635
    
    
    
    
    
    
