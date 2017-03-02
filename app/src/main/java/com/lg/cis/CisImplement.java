package com.lg.cis;

/**
 * Created by zhao on 2016/7/4.
 */
public class CisImplement {
    //原始语音数据
    private short[] src;
    private int srcLength;
    //预加重以后的数据
    private double[] yujiazhong;
    //经过低通滤波后的数据
    private double[] LpfFiltered;
    //整流后的数据
    private double[] zhengliu;
    //输出
    private short[] out;


    //***************************************************************************
    //带通滤波系数
    /**
     * 这些参数是通过MATLAB仿真来获得的
     */

    double daitongb[][] = {{0.0000322, 0, -0.000096, 0, 0.0000965, 0, -0.000032}, {0.0000993, 0, -0.0003, 0, 0.000298, 0, -0.000099},
            {0.00027, 0, -0.00081, 0, 0.000809, 0, -0.00027}, {0.000734, 0, -0.0022, 0, 0.002201, 0, -0.00073}, {0.002177, 0, -0.00653, 0, 0.00653, 0, -0.00218},
            {0.005915, 0, -0.01774, 0, 0.017744, 0, -0.00591}, {0.014718, 0, -0.4416, 0, 0.044155, 0, -0.01472}, {0.034092, 0, -0.10288, 0, 0.102277, 0, -0.03409}};
    double daitonga[][] = {{1, -5.811510076, 14.13059141, -18.399486, 13.53143164, -5.329149653, 0.878130136}, {1, -5.689405167, 13.60420862, -17.49640716, 12.76425666, -5.008615383, 0.826025899},
            {1, -5.486857755, 12.77546236, -16.14378385, 11.67465048, -4.582198642, 0.763263024}, {1, -5.126300166, 11.39426732, -14.00088003, 10.02377892, -3.967575592, 0.681114816},
            {1, -4.467240035, 9.11633408, -10.66614051, 7.541512567, -3.057077895, 0.566675059}, {1, -3.329149467, 5.913633998, -6.355375106, 4.493812082, -1.918072918, 0.438554504},
            {1, -1.464037012, 2.581089308, -2.012731715, 1.766099009, -0.665032957, 0.30823304}, {1, 1.200614314, 1.910086686, 1.315029489, 1.1503135, 0.391034915, 0.187685375}};
    //低通滤波系数
    double ditong[][] = {{0.0000312, 0.000125, 0.0001874, 0.000125, 0.0000312}, {1, -3.5897, 4.8513, -2.9241, 0.663}};
    //****************************************************************************


    //构造函数
    CisImplement(short[] src, int srcLength) {
        this.src = src;
        this.srcLength = srcLength;

    }

    /**
     * CIS算法
     * @return
     */
    public short[] BeginCis() {

        /**预加重y(n) = x(n) - 0.95 * x(n-1)*/
        yujiazhong = new double[srcLength];

        for (int i = 1; i < srcLength; i++) {
            yujiazhong[i] = src[i] - 0.95 * src[i - 1];
        }
        /**6阶带通滤波 ---8个通道*/
        /**
         * 这些数据是原始的数据经过带通滤波以后产生的数据
         */

        //Channel 1*************************************************
        //Wp1=[300 432]*2/Fs;Ws1=[280 452]*2/Fs;Rp1=1;Rs1=3;
        double[] BpfFiltered1 = BpfFilter(yujiazhong, 0);
//        //Channel 2**************************************************
        //wp2=[432 620];ws2=[410 660];Rp2=1;Rs2=3;
        double[] BpfFiltered2 = BpfFilter(yujiazhong, 1);
//        //Channel 3**************************************************
        //wp3=[620 894];ws3=[600 918];Rp3=1;Rs3=2;
        double[] BpfFiltered3 = BpfFilter(yujiazhong, 2);
//        //Channel 4**************************************************
        //wp4=[894 1284];ws4=[850 1354];Rp4=1;Rs4=3.3;
        double[] BpfFiltered4 = BpfFilter(yujiazhong, 3);
//        //Channel 5**************************************************
        //wp5=[1284 1848];ws5=[1200 1940];Rp5=1;Rs5=3;
        double[] BpfFiltered5 = BpfFilter(yujiazhong, 4);
//        //Channel 6**************************************************
        //wp6=[1848 2656];ws6=[1785 2756];Rp6=1;Rs6=2;
        double[] BpfFiltered6 = BpfFilter(yujiazhong, 5);
//        //Channel 7**************************************************
        //wp7=[2656 3826];ws7=[2556 3926];Rp7=1;Rs7=2;
        double[] BpfFiltered7 = BpfFilter(yujiazhong, 6);
//        //Channel 8**************************************************
        //wp8=[3826 5500];ws8=[3756 5700];Rp8=1;Rs8=1.5;
        double[] BpfFiltered8 = BpfFilter(yujiazhong, 7);

        /**全波整流*/
        /**
         * 采用了平方根的形式来进行全波整流
         */
        for (int i = 0; i < srcLength; i++) {
            BpfFiltered1[i] = Math.abs(BpfFiltered1[i]);
            BpfFiltered2[i] = Math.abs(BpfFiltered2[i]);
            BpfFiltered3[i] = Math.abs(BpfFiltered3[i]);
            BpfFiltered4[i] = Math.abs(BpfFiltered4[i]);
            BpfFiltered5[i] = Math.abs(BpfFiltered5[i]);
            BpfFiltered6[i] = Math.abs(BpfFiltered6[i]);
            BpfFiltered7[i] = Math.abs(BpfFiltered7[i]);
            BpfFiltered8[i] = Math.abs(BpfFiltered8[i]);

        }
        /**各通道低通*/
//        double[] LpfFiltered1 = LpfFilter(BpfFiltered1);
//        double[] LpfFiltered2 = LpfFilter(BpfFiltered2);
//        double[] LpfFiltered3 = LpfFilter(BpfFiltered3);
//        double[] LpfFiltered4 = LpfFilter(BpfFiltered4);
//        double[] LpfFiltered5 = LpfFilter(BpfFiltered5);
//        double[] LpfFiltered6 = LpfFilter(BpfFiltered6);
//        double[] LpfFiltered7 = LpfFilter(BpfFiltered7);
//        double[] LpfFiltered8 = LpfFilter(BpfFiltered8);

        /**非线性压缩*/


        //double转short***********************************************
        out = new short[srcLength];
        for (int i = 0; i < srcLength; i++) {
            /**
             * 这里输出的是七个带通滤波通道以后的数据
             * 这里的话，我们是可以对输出进行修改的。
             */
            //out[i] = (short) BpfFiltered7[i];
            out[i] =(short)yujiazhong[i];
        }
        return out;
    }

    /**
     *
     * @param preLpf  经过预加重以后的数据
     * @return
     */
    private double[] LpfFilter(double[] preLpf) {
        //        //4阶低通滤波*************************************************
        //Fp=400;Fc=800;Rp=1;Rs=15;
        LpfFiltered = new double[srcLength];
        double[] LpfFilter1 = new double[srcLength];
        double[] LpfFilter2 = new double[srcLength];
        for (int i = 4; i < srcLength; i++) {

            LpfFilter1[i] = ditong[0][0] * preLpf[i - 0] + ditong[0][1] * preLpf[i - 1] + ditong[0][2] * preLpf[i - 2]
                    + ditong[0][3] * preLpf[i - 3] + ditong[0][4] * preLpf[i - 4];
            LpfFilter2[i] = ditong[1][1] * LpfFiltered[i - 1] + ditong[1][2] * LpfFiltered[i - 2] + ditong[1][3] * LpfFiltered[i - 3]
                    + ditong[1][4] * LpfFiltered[i - 4];

            LpfFiltered[i] = LpfFilter1[i] - LpfFilter2[i];
        }
        return LpfFiltered;
    }

    /**
     * 带通滤波，
     * @param preBpf 经过预加重以后的语音数据
     * @param ChannelId  是第几个带通的通道
     * @return
     */
    private double[] BpfFilter(double[] preBpf, int ChannelId) {
        double[] BpfFiltered = new double[srcLength];
        double[] BpfFilter1 = new double[srcLength];
        double[] BpfFilter2 = new double[srcLength];
        for (int i = 6; i < srcLength; i++){
            BpfFilter1[i] =daitongb[ChannelId][0] * preBpf[i - 0] + daitongb[ChannelId][1] * preBpf[i - 1] + daitongb[ChannelId][2] * preBpf[i - 2]
                    + daitongb[ChannelId][3] * preBpf[i - 3] + daitongb[ChannelId][4] * preBpf[i - 4] + daitongb[ChannelId][5] * preBpf[i - 5] + daitongb[ChannelId][6] * preBpf[i - 6];
            BpfFilter2[i] = daitonga[ChannelId][1] * BpfFiltered[i - 1] + daitonga[ChannelId][2] * BpfFiltered[i - 2] + daitonga[ChannelId][3] * BpfFiltered[i - 3]
                    + daitonga[ChannelId][4] * BpfFiltered[i - 4] + daitonga[ChannelId][5] * BpfFiltered[i - 5] + daitonga[ChannelId][6] * BpfFiltered[i - 6];
            BpfFiltered[i] = BpfFilter1[i] - BpfFilter2[i];
        }
        return BpfFiltered;
    }

}
