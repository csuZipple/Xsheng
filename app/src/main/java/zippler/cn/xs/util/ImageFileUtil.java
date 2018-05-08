package zippler.cn.xs.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Zipple on 2018/5/8.
 * help to get pictures
 */
public class ImageFileUtil {
    /**
     * 从sd卡获取图片资源
     * @return
     */
    public static List<String> getImagesInPath(String filePath) {
        List<String> imagePathList = new ArrayList<>();
        File fileAll = new File(filePath);
        File[] files = fileAll.listFiles();
        for (File file : files) {
            if (checkIsImageFile(file.getPath())) {
                imagePathList.add(file.getAbsolutePath());
            }
        }
        return imagePathList;
    }

    /**
     * check the extension name
     * @param fName  file name
     * @return is or not
     */
    private static boolean checkIsImageFile(String fName) {
        boolean isImageFile = false;
        // 获取扩展名
        String FileEnd = fName.substring(fName.lastIndexOf(".") + 1,
                fName.length()).toLowerCase();
        isImageFile = FileEnd.equals("jpg") || FileEnd.equals("png") || FileEnd.equals("gif")
                || FileEnd.equals("jpeg") || FileEnd.equals("bmp");
        return isImageFile;
    }

}
