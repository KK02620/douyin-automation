package com.example.douyinautomation.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.example.douyinautomation.utils.DouyinUIHelper;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DouyinAccessibilityService extends AccessibilityService {
    private static final String TAG = "DouyinAccessibility";
    private static final String DOUYIN_PACKAGE = "com.ss.android.ugc.aweme";
    private static final String PREFS_NAME = "DouyinAutomationPrefs";
    
    private static final String KEY_LIKE = "auto_like";
    private static final String KEY_FOLLOW = "auto_follow";
    private static final String KEY_COMMENT = "auto_comment";
    private static final String KEY_SCROLL = "auto_scroll";
    private static final String KEY_COMMENTS = "comments_list";
    
    private boolean autoLike = true;
    private boolean autoFollow = false;
    private boolean autoComment = false;
    private boolean autoScroll = true;
    private String[] commentsList = {"很棒！", "真不错", "支持一下", "点赞了"};
    
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    
    private boolean isProcessing = false;
    private int currentVideoCount = 0;
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 只处理抖音应用的事件
        if (event.getPackageName() == null || !event.getPackageName().toString().equals(DOUYIN_PACKAGE)) {
            return;
        }
        
        // 读取设置
        loadSettings();
        
        // 根据事件类型处理
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (!isProcessing) {
                    processCurrentScreen();
                }
                break;
        }
    }
    
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务被中断");
    }
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "无障碍服务已连接");
        loadSettings();
    }
    
    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        autoLike = preferences.getBoolean(KEY_LIKE, true);
        autoFollow = preferences.getBoolean(KEY_FOLLOW, false);
        autoComment = preferences.getBoolean(KEY_COMMENT, false);
        autoScroll = preferences.getBoolean(KEY_SCROLL, true);
        
        String commentsStr = preferences.getString(KEY_COMMENTS, "很棒！\n真不错\n支持一下\n点赞了");
        commentsList = commentsStr.split("\n");
    }
    
    private void processCurrentScreen() {
        isProcessing = true;
        
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            isProcessing = false;
            return;
        }
        
        try {
            // 检测当前是否在视频播放界面
            if (DouyinUIHelper.isInVideoPlayingScreen(rootNode)) {
                currentVideoCount++;
                Log.d(TAG, "正在处理第 " + currentVideoCount + " 个视频");
                
                // 延迟执行操作，模拟人工操作
                int delay = 1000 + random.nextInt(2000);
                
                // 自动点赞
                if (autoLike) {
                    handler.postDelayed(() -> performLikeAction(getRootInActiveWindow()), delay);
                    delay += 1000 + random.nextInt(1000);
                }
                
                // 自动关注
                if (autoFollow) {
                    handler.postDelayed(() -> performFollowAction(getRootInActiveWindow()), delay);
                    delay += 1000 + random.nextInt(1000);
                }
                
                // 自动评论
                if (autoComment) {
                    handler.postDelayed(() -> performCommentAction(getRootInActiveWindow()), delay);
                    delay += 2000 + random.nextInt(2000);
                }
                
                // 自动滑动到下一个视频
                if (autoScroll) {
                    handler.postDelayed(this::scrollToNextVideo, delay + 3000 + random.nextInt(3000));
                }
            }
        } finally {
            rootNode.recycle();
            isProcessing = false;
        }
    }
    
    private void performLikeAction(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return;
        
        try {
            // 使用DouyinUIHelper查找点赞按钮
            AccessibilityNodeInfo likeButton = DouyinUIHelper.findNodeByType(rootNode, "like_button");
            
            if (likeButton != null) {
                likeButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "已点赞");
            } else {
                // 如果找不到点赞按钮，尝试在屏幕右侧进行点击
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Rect windowBounds = new Rect();
                    rootNode.getBoundsInScreen(windowBounds);
                    clickAtPosition(windowBounds.width() * 0.9f, windowBounds.height() * 0.4f);
                    Log.d(TAG, "通过坐标点赞");
                }
            }
        } finally {
            rootNode.recycle();
        }
    }
    
    private void performFollowAction(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return;
        
        try {
            // 尝试查找关注按钮
            String[] followButtonTexts = {"关注", "+ 关注"};
            
            for (String text : followButtonTexts) {
                List<AccessibilityNodeInfo> nodes = DouyinUIHelper.findNodesByText(rootNode, text);
                if (!nodes.isEmpty()) {
                    nodes.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    Log.d(TAG, "已关注");
                    return;
                }
            }
            
            // 尝试通过ID查找关注按钮
            AccessibilityNodeInfo followButton = DouyinUIHelper.findNodeByType(rootNode, "follow_button");
            if (followButton != null) {
                followButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.d(TAG, "已通过ID关注");
            } else {
                Log.d(TAG, "未找到关注按钮或已关注");
            }
        } finally {
            rootNode.recycle();
        }
    }
    
    private void performCommentAction(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) return;
        
        try {
            // 点击评论按钮
            AccessibilityNodeInfo commentButton = DouyinUIHelper.findNodeByType(rootNode, "comment_button");
            
            if (commentButton != null) {
                commentButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                
                // 延迟操作，等待评论区加载
                handler.postDelayed(() -> {
                    // 点击评论输入框
                    AccessibilityNodeInfo rootNode2 = getRootInActiveWindow();
                    if (rootNode2 == null) return;
                    
                    try {
                        AccessibilityNodeInfo commentEdit = DouyinUIHelper.findNodeByType(rootNode2, "comment_edit");
                        
                        if (commentEdit != null) {
                            commentEdit.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            
                            // 随机选择一条评论
                            String comment = commentsList[random.nextInt(commentsList.length)];
                            
                            // 设置评论文本并发送
                            Bundle arguments = new Bundle();
                            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, comment);
                            commentEdit.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                            
                            // 延迟点击发送按钮
                            handler.postDelayed(() -> {
                                AccessibilityNodeInfo rootNode3 = getRootInActiveWindow();
                                if (rootNode3 == null) return;
                                
                                try {
                                    // 查找发送按钮
                                    AccessibilityNodeInfo sendButton = DouyinUIHelper.findNodeByType(rootNode3, "send_button");
                                    
                                    if (sendButton != null) {
                                        sendButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                        Log.d(TAG, "已评论: " + comment);
                                        
                                        // 评论发送后返回视频
                                        handler.postDelayed(() -> performGlobalAction(GLOBAL_ACTION_BACK), 1000);
                                    }
                                } finally {
                                    rootNode3.recycle();
                                }
                            }, 1000);
                        }
                    } finally {
                        rootNode2.recycle();
                    }
                }, 1500);
            }
        } finally {
            rootNode.recycle();
        }
    }
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void clickAtPosition(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        
        dispatchGesture(gestureBuilder.build(), null, null);
    }
    
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void scrollToNextVideo() {
        // 获取屏幕尺寸
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        
        try {
            Rect windowBounds = new Rect();
            rootNode.getBoundsInScreen(windowBounds);
            
            int screenHeight = windowBounds.height();
            int screenWidth = windowBounds.width();
            
            // 从屏幕中间底部向上滑动
            Path path = new Path();
            path.moveTo(screenWidth / 2, screenHeight * 0.8f);
            path.lineTo(screenWidth / 2, screenHeight * 0.2f);
            
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 300));
            
            dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d(TAG, "滑动到下一个视频");
                }
                
                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d(TAG, "滑动被取消");
                }
            }, null);
        } finally {
            rootNode.recycle();
        }
    }
}