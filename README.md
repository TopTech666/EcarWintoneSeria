# EcarWintoneSeria

使用教程
===================================  

   1.初始化：
       1）获取序列号
       2）授权 -网络请求在api内进行
          WintonRecogManager.getInstance().authInit(Activity, true);//序列号获取成功后自动保存序列号
         
   2.识别Manager WintonRecogManager
       1）private static AuthHelper authHelper;  序列号是静态属性
       2）调用WintonRecogManager类的useWTRecognitionByData（...）方法进行识别--视频识别
       3）调用WintonRecogManager类的useWTRecognitionByPic（...）方法进行识别--图片识别
       4）识别最短时间间隔设置
          private long RECOG_SPACING = 300;//识别过滤间隔  单位ms

           public void setRecogSpacing(long RECOG_SPACING) {
               this.RECOG_SPACING = RECOG_SPACING;
           }
       
   3）序列号API
   
      public class SPKeyUtils {
    
       /**
         * 文通识别
         */
        //获取文通序列号
        private static final String s_SERIAL_NUM = "serialNum";
    
        //获取序列号
        public static String getSeriaNum(Context mContext){
       return (String) SPUtils.get(mContext,s_SERIAL_NUM, "");
        }
        //保存序列号
        public static void  saveSeriaNum(Context mContext, String seriaNum){
        SPUtils.put(mContext,s_SERIAL_NUM, seriaNum);

        }

        //清除序列号
       public static void  clearSeriaNum(Context mContext){
            SPUtils.put(mContext,s_SERIAL_NUM, "");
        }
    }


引用方法 需要排除EcarEncryption包
------------------------------------
     compile('com.github.goEcar:EcarWintoneSeria:latest.release') {
            exclude group: 'com.github.goEcar', module: 'EcarEncryption'
        }


需要注意以下几点：
      1. PlateRecognitionParameter 创建时参数的赋值顺序一定要按线面的顺序赋值(5.1及以上系统)
                       //识别参数设置
                      PlateRecognitionParameter mPlateRecParam = new PlateRecognitionParameter();

                      //设置识别区域
                      mPlateRecParam.height = preHeight;
                      mPlateRecParam.width = preWidth;
                      mPlateRecParam.picByte = data;
     2.
       

       
       
