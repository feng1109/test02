package com.eseasky.modules.space.config;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.eseasky.common.code.utils.BusinessException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DrawQRImage {

    private int avg = 100;
    private int padding = 10; // 内边距

    private Font init() {
        InputStream resourceAsStream = null;
        try {
            String path = "/font/simsun.ttc";
            resourceAsStream = this.getClass().getResourceAsStream(path);
            return Font.createFont(Font.TRUETYPE_FONT, resourceAsStream);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("DrawQRImage-init，初始化字体失败！");
        } finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
        return null;
    }

    /**
     * 
     * @param file 需要生成的图片
     * @param qrfile 已经创建好的二维码图片
     * @param school 租户名称
     * @param buildFloorRoom 综合楼+楼层+房间
     * @param seatNum 座位编号
     * @throws Exception
     */
    public void creat(File file, File qrfile, String school, String buildFloorRoom, String seatNum) throws Exception {
        int width = 800;
        int height = 500;

        Font toUse = init();
        if (toUse == null) {
            throw BusinessException.of("获取字体失败！");
        }

        // 默认黑色背景，画一个淡蓝色的背景
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setBackground(new Color(30, 144, 255));
        g2.clearRect(0, 0, width, height);// 用上面的颜色来填充指定的矩形。

        // 在淡蓝色的背景上画一个内边距为 border 的白色面板
        g2.setPaint(Color.white);// 设置画笔,设置Paint属性
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// 抗锯齿
        g2.fill(new RoundRectangle2D.Double(padding, padding, width - padding * 2, height - padding * 2, padding, padding));


        // 10个点的像素是90，90*8=720，剩余80像素左右各40。
        g2.setPaint(new Color(30, 144, 255));
        g2.setFont(toUse.deriveFont(0, 18));
        for (int i = 0; i < 8; i++) {
            g2.drawString("..........", 40 + 90 * i, avg + padding + 8);
        }

        // x写死,y写死
        g2.setColor(Color.black);
        g2.setFont(toUse.deriveFont(0, 20));
        for (int i = 0; i < 19; i++) {
            g2.drawString("|", (width - 6) / 2, avg * 2 + padding + 15 + (10 * i));
        }

        // 绘制二维码，将二维码的长和宽改为 avg * 2
        BufferedImage bufferedImage = ImageIO.read(qrfile);
        g2.drawImage(bufferedImage.getScaledInstance(avg * 2, avg * 2, Image.SCALE_DEFAULT), avg * 5, avg * 2 + padding, null);


        // “武汉大学”
        g2.setPaint(Color.black);
        school = StringUtils.join(school.trim().split(""), " ");
        toUse = toUse.deriveFont(0, 50);
        g2.setFont(toUse);
        ImageProp prop = getProp(toUse, school);
        g2.drawString(school, (int) (width - prop.getWidth()) / 2, 50 + padding + 19); // x计算，y写死

        // “行政楼二楼A302室”，x写死，y写死
        g2.setColor(Color.black);
        g2.setFont(toUse.deriveFont(0, 26));
        g2.drawString(buildFloorRoom, 40, avg + padding + 26 * 2);


        // “A302”，x计算，y写死
        g2.setColor(Color.black);
        toUse = toUse.deriveFont(0, 90);
        g2.setFont(toUse);
        prop = getProp(toUse, seatNum);
        g2.drawString(seatNum, (width / 2 - prop.getWidth()) / 2, 300 + padding + 30);

        g2.dispose();
        ImageIO.write(bi, "png", file);
    }


    private ImageProp getProp(Font font, String text) {
        Graphics2D graphics = new BufferedImage(800, 500, BufferedImage.TYPE_4BYTE_ABGR).createGraphics();
        graphics.setFont(font);
        FontRenderContext context = graphics.getFontRenderContext();
        Rectangle2D stringBounds = font.getStringBounds(text, context);

        ImageProp prop = new ImageProp();
        prop.setWidth((int) stringBounds.getWidth());
        return prop;
    }

}


@Data
class ImageProp {
    private int width;
}
