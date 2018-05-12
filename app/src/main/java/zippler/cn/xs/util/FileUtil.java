package zippler.cn.xs.util;

import android.util.Log;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

/**
 * Created by Zipple on 2018/5/12.
 * For write txt in file which can help to concat videos.
 */

class FileUtil {
    private static final String TAG = "FileUtil";
    static boolean writeContentsToTxt(String path, List<String> content){
        boolean result = false;
        String strContent = "";
        for(int i = 0; i < content.size(); ++i) {
            strContent += "file " + content.get(i) + "\r\n";
        }
        try {
            File file = new File(path);
            if(file.exists()) {
                Log.d(TAG, "writeContentsToTxt: concat file exist");
                if (file.delete()){
                    Log.d(TAG, "writeContentsToTxt: deleted concat file success");
                }else{
                    Log.e(TAG, "writeContentsToTxt: deleted concat file failed");
                }
            }else{
                Log.d(TAG, "writeContentsToTxt: concat file is not exist.");
                if (file.createNewFile()){
                    RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                    raf.seek(file.length());
                    raf.write(strContent.getBytes());
                    raf.close();
                    Log.d(TAG, "writeContentsToTxt: create concat file success");
                    result = true;
                }else{
                    Log.e(TAG, "writeContentsToTxt: create concat file failed");
                }
            }
        } catch (Exception var7) {
            Log.e("TestFile", "Error on write File:" + var7);
        }
        return result;
    }
}
