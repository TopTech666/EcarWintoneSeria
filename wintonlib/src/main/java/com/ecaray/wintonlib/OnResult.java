package com.ecaray.wintonlib;

/**
 * ===============================================
 * <p>
 * 类描述:
 * <p>
 * 创建人: Eric_Huang
 * <p>
 * 创建时间: 2016/11/2 15:49
 * <p>
 * 修改人:Eric_Huang
 * <p>
 * 修改时间: 2016/11/2 15:49
 * <p>
 * 修改备注:
 * <p>
 * ===============================================
 */
public interface OnResult {
    void onGeted(String fileName, String number);  //获取了结果
    String saveImage(byte[] data);
}
