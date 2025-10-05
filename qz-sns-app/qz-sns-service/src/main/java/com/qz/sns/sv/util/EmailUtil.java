package com.qz.sns.sv.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class EmailUtil {
    @Value("${spring.mail.username:qzreadapp@163.com}")
    private String from;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    private JavaMailSender sender;
 
    public boolean sendMail(String to,String subject, String body){
        System.out.println("EmailUtil.sendMail 开始执行");
        System.out.println("收件人：" + to);
        System.out.println("主题：" + subject);
        
        if (to == null || to.equals("")){
            System.out.println("收件人邮箱为空，发送失败");
            return false;
        }
        
        try {
            //构建标准的简单邮件信息
            //发送人和xml保持一致
            SimpleMailMessage m=new SimpleMailMessage();
            //发送人
            m.setFrom(from);
            System.out.println("发件人：" + from);
            //接收人
            m.setTo(to);
            //邮件标题
            m.setSubject(subject);
            //内容
            m.setText(body);
            
            System.out.println("准备发送邮件...");
            sender.send(m);//发送邮件
            System.out.println("邮件发送成功！");
            return true;
        } catch (Exception e) {
            System.out.println("邮件发送失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendMail(String to, String subject, String body, List<File> attachments) {
        if (to == null || to.isEmpty()) {
            return false;
        }

        MimeMessage message = sender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true); // true 表示支持多部分邮件
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // 第二个参数表示文本是HTML格式

            if (attachments != null && !attachments.isEmpty()) {
                for (File file : attachments) {
                    helper.addAttachment(file.getName(), file);
                }
            }

            sender.send(helper.getMimeMessage());
            System.out.println("带有附件的邮件发送成功！");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }
    //html太长，使用模板来发送复杂的邮件--比如上传附件的邮件
    public void sendMail(String to, String subject, String templateName, Map<String,Object> params, List<File> files){
        Context context=new Context();
        context.setVariables(params);
        String htmlBody = templateEngine.process("mail.html", context);
        sendMail(to,subject,htmlBody,files);
    }
}