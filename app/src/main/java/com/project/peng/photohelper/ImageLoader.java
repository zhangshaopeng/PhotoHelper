package com.project.peng.photohelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


/**
 * Describe：图片加载类
 * auther：  zhangshaopeng
 * Emile：   1377785991@qq.com
 * Date：    2018/6/18
 */

public class ImageLoader {

    private static ImageLoader mInstance;
    /**
     * 图片缓存的核心对象
     */
    private LruCache<String, Bitmap> mLruCache;

    /**
     * 线程池
     *
     * @return
     */
    private ExecutorService mThreadPool;
    private static final int DEAFULT_THEAD_COUNT = 1;

    /**
     * 队列的调度方式
     *
     * @return
     */
    private Type mType = Type.LIFO;
    /**
     * 任务队列
     */
    private LinkedList<Runnable> mTaskQueue;

    /**
     * 后台轮训线程
     */
    private Thread mPoolThread;
    private Handler mPoolThreadHandler;

    /**
     * 更新UI线程中的handler
     */
    private Handler mUIHandler;
    private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
    private Semaphore mSemaphoreThreadPool;

    public enum Type {
        FIFI, LIFO;
    }

    private ImageLoader(int threadCount, Type type) {
        init(threadCount, type);

    }

    /**
     * 初始化操作
     *
     * @param threadCount
     * @param type
     */
    private void init(int threadCount, Type type) {
        mPoolThread = new Thread() {
            @Override
            public void run() {
                super.run();
                Looper.prepare();

                mPoolThreadHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        //线程池去任务进行执行
                        mThreadPool.execute(getTask());
                        try {
                            mSemaphoreThreadPool.acquire();
                        } catch (InterruptedException e) {

                        }
                    }
                };
                //释放一个信号量
                mSemaphorePoolThreadHandler.release();
                Looper.loop();

            }
        };
        mPoolThread.start();
        //获取我们英勇的最大应用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheMeory = maxMemory / 8;
        mLruCache = new LruCache<String, Bitmap>(cacheMeory) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
        //创建线程池
        mThreadPool = Executors.newFixedThreadPool(threadCount);
        mTaskQueue = new LinkedList<Runnable>();
        mType = type;
        mSemaphoreThreadPool = new Semaphore(threadCount);


    }

    //从任务队列取出一个方法
    private Runnable getTask() {
        if (mType == Type.LIFO) {
            return mTaskQueue.removeFirst();
        } else if (mType == Type.FIFI) {
            mTaskQueue.removeLast();
        }
        return null;
    }

    public static ImageLoader getInstace(int threadCount, Type type) {
        if (mInstance == null) {
            synchronized (ImageLoader.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoader(DEAFULT_THEAD_COUNT, Type.LIFO);
                }
            }
        }
        return mInstance;
    }

    /**
     * 根据path设置图片
     *
     * @param path
     * @param imageView
     */
    public void loadImage(final String path, final ImageView imageView) {

        imageView.setTag(path);
        if (mUIHandler == null) {
            mUIHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //获取得到的图片，设置imageview图片
                    ImgBeanhodler hodler = (ImgBeanhodler) msg.obj;
                    Bitmap bm = hodler.bitmap;
                    ImageView imageView1 = hodler.imageView;
                    final String path = hodler.path;
                    //将path与getTag存储路径进行比较
                    if (imageView.getTag().toString().equals(path)) {
                        imageView.setImageBitmap(bm);
                    }
                }

            };
        }
        //根据path从缓存中获取bitmap
        Bitmap bm = getBitmapFromCache(path);
        if (bm != null) {
            refreashBitmap(path, imageView, bm);
        } else {
            addTasks(new Runnable() {
                @Override
                public void run() {
                    //加载图片
                    //1：获取图片需要的大小
                    ImageSize imageSize = getImageViewSize(imageView);
                    //2：压缩图片
                    Bitmap bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
                    //把图片加入到缓存
                    addBitmapToLruCache(path, bm);
                    //回掉图片
                    refreashBitmap(path, imageView, bm);
                    mSemaphoreThreadPool.release();
                }
            });
        }


    }

    private void refreashBitmap(String path, ImageView imageView, Bitmap bm) {
        Message message = Message.obtain();
        ImgBeanhodler hodler = new ImgBeanhodler();
        hodler.bitmap = bm;
        hodler.imageView = imageView;
        hodler.path = path;
        message.obj = hodler;
        mUIHandler.sendMessage(message);
    }

    /**
     * 图片加入缓存LruCache
     *
     * @param path
     * @param bm
     */
    protected void addBitmapToLruCache(String path, Bitmap bm) {
        if (getBitmapFromCache(path) == null) {
            if (bm != null) {
                mLruCache.put(path, bm);
            }
        }
    }

    /**
     * 压缩根据展示图片的宽高
     *
     * @param path
     * @param width
     * @param height
     * @return
     */
    protected Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
        //获取图片的宽和高，并不把图片加载到内存中
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        //获取图片型号
        options.inSampleSize = caculateInSampleSize(options, width, height);

        //使用获取到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);


        return bitmap;
    }

    /**
     * 根据需求的宽高以及实际的宽高计算SampleSize
     *
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int width = options.outWidth;
        int height = options.outHeight;

        int inSampleSize = 1;
        if (width > reqWidth || height > reqHeight) {
            int widthRadio = Math.round(width * 1.0f / reqWidth);
            int heightRadio = Math.round(height * 1.0f / reqHeight);

            inSampleSize = Math.max(widthRadio, heightRadio);
        }
        return inSampleSize;

    }

    /**
     * 根据ImageVie获取适当的压缩的宽高
     *
     * @param imageView
     * @return
     */

    protected ImageSize getImageViewSize(ImageView imageView) {
        ImageSize imageSize = new ImageSize();
        DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();

        ViewGroup.LayoutParams lp = imageView.getLayoutParams();
        int width = imageView.getWidth();
        // int width = (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT ? 0 : imageView.getWidth());
        if (width <= 0) {
            width = lp.width;//获取image在layout中声明的宽高
        }
        if (width <= 0) {
            width = getImageViewFileValue(imageView, "mMaxWidth");//检查最大值
            ;//检查最大值
        }
        if (width <= 0) {
            width = displayMetrics.widthPixels;//设置屏幕宽度
        }

        int height = imageView.getHeight();
        if (height <= 0) {
            height = lp.height;//获取image在layout中声明的宽高
        }
        if (height <= 0) {
            height = getImageViewFileValue(imageView, "mMaxHeight");//检查最大值
            ;//检查最大值
        }
        if (height <= 0) {
            height = displayMetrics.heightPixels;
        }
        return imageSize;
    }

    private static int getImageViewFileValue(Object object, String
            filedName) {
        int value = 0;

        try {
            Field field = ImageView.class.getDeclaredField(filedName);
            field.setAccessible(true);
            int fieldValue = field.getInt(object);
            if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                value = fieldValue;
            }

        } catch (Exception e) {

        }
        return value;
    }

    private synchronized void addTasks(Runnable runnable) {
        mTaskQueue.add(runnable);
        try {
            if (mPoolThreadHandler == null) {
                mSemaphorePoolThreadHandler.acquire();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mPoolThreadHandler.sendEmptyMessage(0x110);
    }


    private Bitmap getBitmapFromCache(String key) {
        return mLruCache.get(key);

    }

    private class ImageSize {
        int width;
        int height;

    }

    private class ImgBeanhodler {
        Bitmap bitmap;
        ImageView imageView;
        String path;
    }

}
