package com.zywl.utils;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.msp.cpdb.lfasr.client.LfasrClientImp;
import com.iflytek.msp.cpdb.lfasr.exception.LfasrException;
import com.iflytek.msp.cpdb.lfasr.model.LfasrType;
import com.iflytek.msp.cpdb.lfasr.model.Message;
import com.iflytek.msp.cpdb.lfasr.model.ProgressStatus;

import java.io.File;
import java.util.HashMap;

/**
 * @packagename com.iflytek.voicecloud.lfasr.demo
 * @projectname lfasr-demo
 */
public class XunFei {
    private static final LfasrType type = LfasrType.LFASR_STANDARD_RECORDED_AUDIO;
    // 等待时长（秒）
    private static int sleepSecond = 1;


    /**
     * @param filePath      带转写音频文件路径
     * @param language      发音语种[cn,en]默认cn
     * @param speakerNumber 发音人数,0-10，0表示盲分, 默认1人，
     * @return
     */
    public static String toTxt(String filePath, Integer speakerNumber, String language) {
        if (language == null || "".equals(language)) {
            language = "cn";
        }
        if (speakerNumber == null || speakerNumber > 10 || speakerNumber < 0) {
            speakerNumber = 1;
        }
        String result = null;
        LfasrClientImp lc = null;
        try {
            lc = LfasrClientImp.initLfasrClient();
        } catch (LfasrException e) {
            // 初始化异常，解析异常描述信息
            Message initMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + initMsg.getErr_no());
            System.out.println("failed=" + initMsg.getFailed());
        }

        // 获取上传任务ID
        String task_id = "";
        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("has_participle", "true");
        params.put("speaker_number", speakerNumber.toString());
        params.put("language", language);

        try {
            // 上传音频文件
            Message uploadMsg = lc.lfasrUpload(filePath, type, params);

            // 判断返回值
            int ok = uploadMsg.getOk();
            if (ok == 0) {
                // 创建任务成功
                task_id = uploadMsg.getData();
            } else {
                // 创建任务失败-服务端异常
                System.out.println("ecode=" + uploadMsg.getErr_no());
                System.out.println("failed=" + uploadMsg.getFailed());
            }
        } catch (LfasrException e) {
            // 上传异常，解析异常描述信息
            Message uploadMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + uploadMsg.getErr_no());
            System.out.println("failed=" + uploadMsg.getFailed());
        }

        // 循环等待音频处理结果
        while (true) {
            try {
                // 等待10s在获取任务进度
                Thread.sleep(sleepSecond * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                // 获取处理进度
                Message progressMsg = lc.lfasrGetProgress(task_id);

                // 如果返回状态不等于0，则任务失败
                if (progressMsg.getOk() != 0) {
                    System.out.println("task was fail. task_id:" + task_id);
                    System.out.println("ecode=" + progressMsg.getErr_no());
                    System.out.println("failed=" + progressMsg.getFailed());
                    return result;
                } else {
                    ProgressStatus progressStatus = JSON.parseObject(progressMsg.getData(), ProgressStatus.class);
                    if (progressStatus.getStatus() == 9) {
                        // 处理完成
                        break;
                    } else {
                        // 未处理完成
                        continue;
                    }
                }
            } catch (LfasrException e) {
                // 获取进度异常处理，根据返回信息排查问题后，再次进行获取
                Message progressMsg = JSON.parseObject(e.getMessage(), Message.class);
                System.out.println("ecode=" + progressMsg.getErr_no());
                System.out.println("failed=" + progressMsg.getFailed());
            }
        }

        // 获取任务结果
        try {
            Message resultMsg = lc.lfasrGetResult(task_id);
            // 如果返回状态等于0，则获取任务结果成功
            if (resultMsg.getOk() == 0) {
                String data = resultMsg.getData();
                result = JsonDataToString(data);
                return result;
            } else {
                // 获取任务结果失败
                System.out.println("ecode=" + resultMsg.getErr_no());
                System.out.println("failed=" + resultMsg.getFailed());
            }
        } catch (LfasrException e) {
            // 获取结果异常处理，解析异常描述信息
            Message resultMsg = JSON.parseObject(e.getMessage(), Message.class);
            System.out.println("ecode=" + resultMsg.getErr_no());
            System.out.println("failed=" + resultMsg.getFailed());
        }
        return result;
    }

    /**
     * @param file 支持文件类型[wav,flac,opus,mp3,m4a]
     * @return
     */
    public static String toTxt(File file) {
        return toTxt(file.getPath());
    }

    /**
     * @param file          支持文件类型[wav,flac,opus,mp3,m4a]
     * @param speakerNumber 发音人数
     * @return
     */
    public static String toTxt(File file, Integer speakerNumber) {
        return toTxt(file.getPath(), speakerNumber, null);
    }

    /**
     * @param filePath      文件路径-支持文件类型[wav,flac,opus,mp3,m4a]
     * @param speakerNumber 发音人数
     * @return
     */
    public static String toTxt(String filePath, Integer speakerNumber) {
        return toTxt(filePath, speakerNumber, null);
    }

    /**
     * @param filePath 文件路径-支持文件类型[wav,flac,opus,mp3,m4a]
     * @return
     */
    public static String toTxt(String filePath) {
        return toTxt(filePath, null, null);
    }

    private static String JsonDataToString(String json) {
        JSONArray objects = JSON.parseArray(json);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.size(); i++) {
            JSONObject jsonObject = objects.getJSONObject(i);
            String onebest = jsonObject.getString("onebest");
            stringBuilder.append(onebest);
        }
        return stringBuilder.toString();
    }
}
