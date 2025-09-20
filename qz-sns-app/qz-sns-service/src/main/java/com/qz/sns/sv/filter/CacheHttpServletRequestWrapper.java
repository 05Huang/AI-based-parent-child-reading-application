package com.qz.sns.sv.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;

/**
 * HTTP请求包装类，用于缓存请求体数据
 * <p>
 * 继承自HttpServletRequestWrapper，通过缓存原始请求的输入流实现请求体的重复读取，
 * 解决HttpServletRequest输入流只能读取一次的问题
 */
public class CacheHttpServletRequestWrapper extends HttpServletRequestWrapper {
    private byte[] requestBody;
    private final HttpServletRequest request;

    /**
     * 构造方法，初始化请求包装器
     * @param request 原始HTTP请求对象，将被包装的请求实例
     */
    public CacheHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
        this.request = request;
    }

    /**
     * 获取缓存的输入流
     * <p>
     * 首次调用时会从原始请求中读取并缓存请求体数据，后续调用直接返回缓存数据的输入流
     * @return ServletInputStream实例，基于缓存的请求体数据
     * @throws IOException 当读取原始输入流发生错误时抛出
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 首次访问时缓存请求体数据
        if (null == this.requestBody) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // 将原始输入流内容复制到内存缓冲区
            IOUtils.copy(request.getInputStream(), baos);
            this.requestBody = baos.toByteArray();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(requestBody);
        // 创建基于缓存数据的输入流实现
        return new ServletInputStream() {//匿名内部类：实现ServletInputStream接口
            @Override
            public boolean isFinished() {//作用：判断是否已经读取完毕，如果已经读取完毕，返回true，否则返回false
                return false;
            }

            @Override
            public boolean isReady() {//作用：判断是否可以读取，如果可以读取，返回true，否则返回false
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {//作用：设置一个监听器，当数据可以读取时，会调用监听器的方法
            }

            @Override
            public int read() {//作用：读取数据，返回读取到的数据，如果没有数据，返回-1，表示读取完毕
                return bais.read();
            }
        };
    }

    /**
     * 获取字符流读取器
     * <p>
     * 基于缓存的输入流创建字符读取器，确保可重复读取请求体内容
     * @return BufferedReader对象，使用缓存的输入流进行构造
     * @throws IOException 当获取输入流发生错误时抛出
     */
    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }
}
