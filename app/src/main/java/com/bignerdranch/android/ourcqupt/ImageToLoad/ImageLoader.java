package com.bignerdranch.android.ourcqupt.ImageToLoad;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.bignerdranch.android.ourcqupt.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 *实现图片缓存的核心类
 */

public class ImageLoader {

    UrlToMD5 instance = UrlToMD5.getInstance();

    private static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_RESULT = 1;

    //设置当前设备的CPU核心数
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    //设置核心线程数
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    //设置最大容量
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    //设置线程闲置超时时长
    private static final long KEEP_ALIVE = 10L;

    private static final int TAG_KEY_URI = R.id.imageLoader_uri;
    //设置缓存的总大小为为50MB，当超出时，DiskLruCache会清除一些缓存
    private static final long DISK_CACHE_SIZE = 1024 * 1024 * 50;
    private static final int IO_BUFFER_SIZE = 8 * 1024;
    private static final int DISK_CACHE_INDEX = 0;
    private boolean mIsDiskLruCacheCreated = false;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {

        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r,"ImageLoader#" + mCount.getAndIncrement());
        }
    };

    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,
            KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),sThreadFactory);

    //直接采用主线程的Looper来构造Handler对象，使得ImageLoader可以在非主线程中构造
    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            LoaderResult result = ( LoaderResult)msg.obj;
            ImageView imageView = result.imageView;
            imageView.setImageBitmap(result.bitmap);
            String uri = (String)imageView.getTag(TAG_KEY_URI);
            //检查图片的url是否发生改变
            if(uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else {
                Log.w(TAG,"set image bitmap,but url has changed,ignored!");
            }
        }
    };

    private Context mContext;
    private ImageResizer mImageResizer = ImageResizer.getInstance();
    private LruCache<String,Bitmap>mMemoryCache;
    private DiskLruCache mDiskLruCache;

    public ImageLoader(Context context){
        mContext = context.getApplicationContext();

        //初始化LruCache
        int maxMemory = (int)(Runtime.getRuntime().maxMemory() / 1024);
        //总容量的大小为当前进程的可用内存的1/8，单位为KB
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            //sizeOf方法用于计算缓存对象的大小，这里的大小的单位要与总容量的单位一致，除以1024是把单位转化为KB
            protected int sizeOf(String key,Bitmap bitmap){
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };

        //创建DiskLruCache
        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if(!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        //判断磁盘剩余空间是否小于磁盘缓存所需的空间大小
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    //从LruCache中添加一个缓存对象
    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if(getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    //从LruCache中获取一个缓存对象
    private Bitmap getBitmapFromMemCache(String key){
        return mMemoryCache.get(key);
    }

    public void bindBitmap(final String uri, final ImageView imageView){
        bindBitmap(uri,imageView,0,0);
    }

    //异步加载接口设计
    //尝试从内存缓存中加载图片，不行则在线程池中调用loadBitmap方法，
    //当图片加载成功后再将图片、图片的地址以及需要绑定的imageView封装成一个LoaderResult对象，
    //然后再通过mMainHandler向主线程发送一个消息
    public Bitmap bindBitmap(final String uri,final ImageView imageView,final int reqWidth,final int reqHeight){
        imageView.setTag(TAG_KEY_URI,uri);
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null){
            imageView.setImageBitmap(bitmap);
        }
        else {
            Runnable loadBitmapTask = new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap = loadBitmap(uri, reqWidth, reqHeight);
                    if (bitmap != null) {
                        LoaderResult result = new LoaderResult(imageView, uri, bitmap);
                        mMainHandler.obtainMessage(MESSAGE_POST_RESULT, result).sendToTarget();
                    }
                }
            };
            THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
        }
        return bitmap;
    }

    //同步加载
    //实现首先从内存缓存中读取图片，再从磁盘缓存中读取图片，最后从网络中拉取图片，这个方法不能再主线程中调用
    public Bitmap loadBitmap(String uri,int reqWidth,int reqHeight){
        Bitmap bitmap = loadBitmapFromMemCache(uri);
        if(bitmap != null){
            Log.d(TAG,"loadBitmapFromMemCache,url:" + uri);
            return bitmap;
        }

        try {
            bitmap = loadBitmapFromDiskCache(uri,reqWidth,reqHeight);
            if(bitmap != null){
                Log.d(TAG,"loadBitmapFromDisk,url:" + uri);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(uri,reqWidth,reqHeight);
            Log.d(TAG,"loadBitmapFromHttp,url:" + uri);
        }catch (IOException e){
            e.printStackTrace();
        }

        if(bitmap == null && !mIsDiskLruCacheCreated){
            Log.w(TAG,"encounter error,DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(uri);
        }

        return bitmap;

    }

    //从内存中获取图片
    private Bitmap loadBitmapFromMemCache(String url){
        final String key = instance.hashKeyFormUrl(url);
        /*final String key = hashKeyFormUrl(url);*/
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    //检查读取缓存图片的环境是否是在主线程，通过检查当前线程的Looper是否为主线程的Looper来判断当前线程是否是主线程，
    // 如果不是主线程就直接抛出异常终止程序
    private Bitmap loadBitmapFromHttp(String url,int reqWidth,int reqHeight)
        throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if(mDiskLruCache == null){
            return null;
        }

        //将图片的url转化为key
        String key = instance.hashKeyFormUrl(url);
        /*String key = hashKeyFormUrl(url);*/
        //Editor表示一个缓存对象的编辑对象
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if(editor != null){
            //返回一个新的Editor对象并得到一个文件输出流
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if(downloadUrlToStream(url,outputStream)){
                //提交写入操作
                editor.commit();
            }else {
                //回退整个操作
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    //从磁盘缓存中查找图片
    private Bitmap loadBitmapFromDiskCache(String url,int reqWidth,int reqHeight)throws IOException{
        if(Looper.myLooper() == Looper.getMainLooper()){
            Log.w(TAG,"load bitmap from UI Thread,it's not recommended!");
        }
        if (mDiskLruCache == null){
            return null;
        }

        Bitmap bitmap = null;
        //将图片的url转换位key
        String key = instance.hashKeyFormUrl(url);
        /*String key = hashKeyFormUrl(url);*/
        //通过get方法得到一个Snapshot对象，再通过该对象得到缓存的文件输入流，从而得到Bitmap对象
        DiskLruCache.Snapshot snapShot =  mDiskLruCache.get(key);
        if(snapShot != null){
            FileInputStream fileInputStream = (FileInputStream)snapShot.getInputStream(DISK_CACHE_INDEX);
            //获得文件流对应的描述符
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            //加载一张缩放后的图片
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
            if(bitmap != null){
                addBitmapToMemoryCache(key,bitmap);
            }
        }

        return bitmap;
    }

    //从网络上下载图片，并通过这个文件输出流写入到文件系统上
    public boolean downloadUrlToStream(String urlString,OutputStream outputStream){
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1){
                out.write(b);
            }
            in.close();
            out.close();
            return true;
        }catch (IOException e){
            Log.e(TAG,"downloadBitmap failed." + e);
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            /*MyUtils.close(out);
            MyUtils.close(in);*/
        }
        return false;
    }

    private Bitmap downloadBitmapFromUrl(String urlString){
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection)url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
            in.close();
        }catch (final IOException e){
            Log.e(TAG,"Error in downloadBitmap:" + e);
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
            /*MyUtils.close(in);*/
        }
        return bitmap;
    }

    /*//将图片的url转成key
    private String hashKeyFormUrl(String url){
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        }catch (NoSuchAlgorithmException e){
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    //将图片的url转成key
    private String bytesToHexString(byte[] bytes){
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<bytes.length;i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if(hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
*/
    @TargetApi(Build.VERSION_CODES.FROYO)
    public File getDiskCacheDir(Context context, String uniqueName){
        boolean externalStorageAvailable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if(externalStorageAvailable && context.getExternalCacheDir() != null){
            cachePath = context.getExternalCacheDir().getPath();
        }else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private long getUsableSpace(File path){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            return path.getUsableSpace();
        }
        final StatFs stats = new StatFs(path.getPath());
        return (long)stats.getBlockSize() * (long)stats.getAvailableBlocks();
    }

    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView,String uri,Bitmap bitmap){
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }

}
