package com.example.common;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 图片相似性检测工具类
 *
 * 提供两种互补的图片指纹算法：
 *
 * 1. pHash（感知哈希）—— 基于图片结构特征
 *    原理：原图 → 缩放32×32 → 灰度化 → DCT变换 → 取低频8×8 → 均值二值化 → 64位哈希
 *    优点：对缩放、轻微色调变化鲁棒
 *    比对：汉明距离（异或后数1的个数），距离越小越相似
 *
 * 2. 颜色直方图 —— 基于图片颜色分布特征
 *    原理：将RGB各通道量化为4级，统计64个颜色桶的归一化分布
 *    优点：对裁剪操作鲁棒（裁剪不改变颜色比例）
 *    比对：余弦相似度，值越接近1越相似
 *
 * 纯 Java 实现，仅依赖 JDK 的 BufferedImage + javax.imageio，无需第三方库。
 */
public class ImageHashUtil {

    /** 最终哈希矩阵的尺寸（8×8 = 64位哈希） */
    private static final int HASH_SIZE = 8;
    /** DCT变换前的图片缩放尺寸（32×32） */
    private static final int RESIZE = 32;

    /**
     * 计算图片的感知哈希值（pHash）
     *
     * 算法步骤：
     * 1. 将原图缩放为 32×32 灰度图（消除尺寸和颜色差异）
     * 2. 对灰度矩阵做 2D DCT（离散余弦变换），将空间域转为频率域
     * 3. 取左上角 8×8 低频区域（代表图片的整体结构，忽略细节噪声）
     * 4. 计算低频区域均值（跳过[0][0]直流分量，它只代表整体亮度）
     * 5. 每个DCT系数与均值比较：大于均值=1，否则=0，生成64位哈希
     *
     * @param imageFile 图片文件
     * @return 64位感知哈希值
     */
    public static long pHash(File imageFile) throws IOException {
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw new IOException("无法读取图片: " + imageFile.getPath());
        }

        // Step 1: 缩放为 32×32 灰度图
        BufferedImage resized = new BufferedImage(RESIZE, RESIZE, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, RESIZE, RESIZE, null);
        g.dispose();

        // Step 2: 提取灰度矩阵（0-255）
        double[][] gray = new double[RESIZE][RESIZE];
        for (int y = 0; y < RESIZE; y++) {
            for (int x = 0; x < RESIZE; x++) {
                gray[y][x] = resized.getRaster().getSample(x, y, 0);
            }
        }

        // Step 3: 2D DCT 变换（空间域 → 频率域）
        double[][] dct = dct2d(gray);

        // Step 4: 取左上角 8×8 低频区域，计算均值（跳过[0][0]直流分量）
        double sum = 0;
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                if (y == 0 && x == 0) continue; // 直流分量只代表整体亮度，不参与哈希
                sum += dct[y][x];
            }
        }
        double avg = sum / (HASH_SIZE * HASH_SIZE - 1);

        // Step 5: 生成64位哈希值（大于均值=1，否则=0）
        long hash = 0;
        for (int y = 0; y < HASH_SIZE; y++) {
            for (int x = 0; x < HASH_SIZE; x++) {
                hash <<= 1;
                if (dct[y][x] > avg) {
                    hash |= 1;
                }
            }
        }
        return hash;
    }

    /**
     * 计算两个 pHash 值的汉明距离
     *
     * 汉明距离 = 两个哈希值逐位异或后 1 的个数
     * 距离越小表示图片越相似（0=完全一致，64=完全不同）
     *
     * @return 汉明距离（0~64）
     */
    public static int hammingDistance(long hash1, long hash2) {
        return Long.bitCount(hash1 ^ hash2);
    }

    /**
     * 计算图片的颜色直方图
     *
     * 将 RGB 三通道各量化为 4 级（0-63, 64-127, 128-191, 192-255），
     * 组成 4×4×4 = 64 个颜色桶，统计每个桶的像素占比。
     *
     * 这种方法对裁剪操作非常鲁棒：裁剪不会改变整体颜色比例，
     * 因此可以检测出简单裁剪后的盗图行为。
     *
     * @param imageFile 图片文件
     * @return 归一化的64维颜色直方图（各元素之和=1）
     */
    public static double[] colorHistogram(File imageFile) throws IOException {
        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw new IOException("无法读取图片: " + imageFile.getPath());
        }

        int bins = 4; // 每个通道量化为4级
        double[] hist = new double[bins * bins * bins]; // 64个颜色桶
        int totalPixels = img.getWidth() * img.getHeight();

        // 遍历所有像素，统计各颜色桶的计数
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                // 将0-255映射到0-3（量化为4级）
                int r = ((rgb >> 16) & 0xFF) * bins / 256;
                int g = ((rgb >> 8) & 0xFF) * bins / 256;
                int b = (rgb & 0xFF) * bins / 256;
                // 三维索引展平为一维：r*16 + g*4 + b
                hist[r * bins * bins + g * bins + b]++;
            }
        }

        // 归一化：计数 → 占比（0~1），消除图片尺寸差异的影响
        for (int i = 0; i < hist.length; i++) {
            hist[i] /= totalPixels;
        }
        return hist;
    }

    /**
     * 计算两个颜色直方图的余弦相似度
     *
     * 公式：cos(θ) = (A·B) / (|A| × |B|)
     * 值域：0~1，越接近1越相似
     *
     * @return 余弦相似度（0~1）
     */
    public static double histogramSimilarity(double[] h1, double[] h2) {
        if (h1.length != h2.length) return 0;
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < h1.length; i++) {
            dot += h1[i] * h2[i];     // 点积（A·B）
            norm1 += h1[i] * h1[i];   // |A|²
            norm2 += h2[i] * h2[i];   // |B|²
        }
        double denominator = Math.sqrt(norm1) * Math.sqrt(norm2);
        return denominator == 0 ? 0 : dot / denominator;
    }

    /**
     * 将直方图数组序列化为逗号分隔的字符串，用于数据库存储
     */
    public static String histToString(double[] hist) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hist.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(String.format("%.6f", hist[i]));
        }
        return sb.toString();
    }

    /**
     * 从逗号分隔的字符串反序列化为直方图数组
     */
    public static double[] histFromString(String str) {
        if (str == null || str.isEmpty()) return new double[64];
        String[] parts = str.split(",");
        double[] hist = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            hist[i] = Double.parseDouble(parts[i]);
        }
        return hist;
    }

    /**
     * 2D 离散余弦变换（DCT-II）
     *
     * 将 n×n 的空间域矩阵转换为频率域矩阵。
     * 左上角为低频分量（代表图片整体结构），右下角为高频分量（代表细节和噪声）。
     *
     * DCT 公式：
     *   F(u,v) = c(u) * c(v) * Σ_x Σ_y f(x,y) * cos((2x+1)uπ/2n) * cos((2y+1)vπ/2n)
     * 其中：
     *   c(0) = 1/√n，c(k) = √(2/n)（k>0）
     *
     * 时间复杂度：O(n⁴)，但由于 n=32 为固定值，实际耗时在毫秒级。
     */
    private static double[][] dct2d(double[][] matrix) {
        int n = matrix.length;
        double[][] result = new double[n][n];

        // 预计算归一化系数
        double[] c = new double[n];
        c[0] = 1.0 / Math.sqrt(n);
        for (int i = 1; i < n; i++) {
            c[i] = Math.sqrt(2.0 / n);
        }

        // 四重循环计算每个频率分量 F(u,v)
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                double sum = 0;
                for (int x = 0; x < n; x++) {
                    for (int y = 0; y < n; y++) {
                        sum += matrix[x][y]
                                * Math.cos((2 * x + 1) * u * Math.PI / (2 * n))
                                * Math.cos((2 * y + 1) * v * Math.PI / (2 * n));
                    }
                }
                result[u][v] = c[u] * c[v] * sum;
            }
        }
        return result;
    }
}
