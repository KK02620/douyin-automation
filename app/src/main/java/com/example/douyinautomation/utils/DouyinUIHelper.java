package com.example.douyinautomation.utils;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抖音UI辅助类，用于处理不同版本抖音的界面元素
 */
public class DouyinUIHelper {
    private static final String TAG = "DouyinUIHelper";
    
    // 抖音常见UI元素ID映射
    private static final Map<String, String[]> UI_ELEMENT_IDS = new HashMap<>();
    
    static {
        // 初始化常见UI元素ID映射
        UI_ELEMENT_IDS.put("like_button", new String[]{
                "com.ss.android.ugc.aweme:id/bz3",
                "com.ss.android.ugc.aweme:id/b59",
                "com.ss.android.ugc.aweme:id/dxx",
                "com.ss.android.ugc.aweme:id/like_button"
        });
        
        UI_ELEMENT_IDS.put("comment_button", new String[]{
                "com.ss.android.ugc.aweme:id/d0l",
                "com.ss.android.ugc.aweme:id/boe",
                "com.ss.android.ugc.aweme:id/c7v",
                "com.ss.android.ugc.aweme:id/comment_button"
        });
        
        UI_ELEMENT_IDS.put("follow_button", new String[]{
                "com.ss.android.ugc.aweme:id/bb1",
                "com.ss.android.ugc.aweme:id/ad3",
                "com.ss.android.ugc.aweme:id/follow_button"
        });
        
        UI_ELEMENT_IDS.put("comment_edit", new String[]{
                "com.ss.android.ugc.aweme:id/a9c",
                "com.ss.android.ugc.aweme:id/et_comment",
                "com.ss.android.ugc.aweme:id/comment_edit_text"
        });
        
        UI_ELEMENT_IDS.put("send_button", new String[]{
                "com.ss.android.ugc.aweme:id/a7c",
                "com.ss.android.ugc.aweme:id/send",
                "com.ss.android.ugc.aweme:id/send_button"
        });
        
        UI_ELEMENT_IDS.put("share_button", new String[]{
                "com.ss.android.ugc.aweme:id/dq8",
                "com.ss.android.ugc.aweme:id/c47",
                "com.ss.android.ugc.aweme:id/share_button"
        });
    }
    
    /**
     * 通过元素类型查找节点
     * 
     * @param rootNode 根节点
     * @param elementType 元素类型，如"like_button"
     * @return 找到的节点，未找到返回null
     */
    public static AccessibilityNodeInfo findNodeByType(AccessibilityNodeInfo rootNode, String elementType) {
        if (rootNode == null || !UI_ELEMENT_IDS.containsKey(elementType)) {
            return null;
        }
        
        String[] ids = UI_ELEMENT_IDS.get(elementType);
        for (String id : ids) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(id);
            if (!nodes.isEmpty()) {
                return nodes.get(0);
            }
        }
        
        return null;
    }
    
    /**
     * 通过文本内容查找节点
     * 
     * @param rootNode 根节点
     * @param text 文本内容
     * @return 找到的节点列表
     */
    public static List<AccessibilityNodeInfo> findNodesByText(AccessibilityNodeInfo rootNode, String text) {
        List<AccessibilityNodeInfo> result = new ArrayList<>();
        findNodesByTextRecursive(rootNode, text, result);
        return result;
    }
    
    /**
     * 递归查找包含指定文本的节点
     */
    private static void findNodesByTextRecursive(AccessibilityNodeInfo node, String text, List<AccessibilityNodeInfo> result) {
        if (node == null) return;
        
        CharSequence nodeText = node.getText();
        CharSequence nodeDesc = node.getContentDescription();
        
        if ((nodeText != null && nodeText.toString().contains(text)) || 
            (nodeDesc != null && nodeDesc.toString().contains(text))) {
            result.add(node);
        }
        
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                findNodesByTextRecursive(child, text, result);
            }
        }
    }
    
    /**
     * 检测当前是否在视频播放界面
     */
    public static boolean isInVideoPlayingScreen(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return false;
        }
        
        // 通过检查点赞、评论等按钮是否存在来判断
        AccessibilityNodeInfo likeButton = findNodeByType(rootNode, "like_button");
        AccessibilityNodeInfo commentButton = findNodeByType(rootNode, "comment_button");
        
        return likeButton != null || commentButton != null;
    }
    
    /**
     * 检测当前是否在评论区界面
     */
    public static boolean isInCommentScreen(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return false;
        }
        
        // 查找评论编辑框
        AccessibilityNodeInfo commentEdit = findNodeByType(rootNode, "comment_edit");
        
        // 或者查找包含"评论"文字的节点
        List<AccessibilityNodeInfo> commentTextNodes = findNodesByText(rootNode, "评论");
        
        return commentEdit != null || !commentTextNodes.isEmpty();
    }
}