package com.lg.cis;

/**
 * Created by liguo on 2016/7/4.
 */

/**
 * Pretreatment类
 *
 * 语音信号预处理类
 */
class Pretreatment
{
    private byte dataBytes[];                   //原始语音数据
    private double dataDouble[];                //预加重后数据
    private int dataFrameNum;                   //帧的数目
    private double dataFrameDouble[][];         //分帧后数据
    public double effectiveFrameDouble[][];     //有效帧数据
    private int effectiveFrameNum;              //有效帧数目
    public double effectiveDouble[];            //有效语音数据，无重叠

    Pretreatment(byte ab[])
    {
        dataBytes=ab;
        preTreat();
    }

    /**
     * Method preTreat
     *
     * 语音信号预处理
     */
    private void preTreat()
    {
        //(预加重)
        int dataByteLength=dataBytes.length;
        dataDouble=new double[dataByteLength];
        for(int i=0;i<dataByteLength;i++)
        {
            dataDouble[i]=dataBytes[i]/*-0.95*dataBytes[i-1]*/;
//          System.out.print(dataDouble[i]);
        }

        //分帧
        dataFrameNum=dataByteLength/120;
        dataFrameDouble=new double[dataFrameNum][240];
//      System.out.println(dataByteLength);
//      System.out.println(dataFrameNum);
        for(int i=0;i/120<dataFrameNum;i+=120)
        {
            for(int j=0;j<240;j++)
            {
                if((i+j)<dataByteLength)
                    dataFrameDouble[i/120][j]=dataDouble[i+j];
            }
        }
/*      for(int i=0;i<dataFrameNum;i++)
        {
            for(int j=0;j<240;j++)
            {
                System.out.print(dataFrameDouble[i][j]);
                System.out.print(" ");
            }
        }*/

        //计算每帧能频值
        double frameLittleEnergy[]=littleEnergy();
        double frameLittleZero[]=littleZero();
        double[] frameEnergyFrequency=new double[dataFrameNum];
        for(int i=0;i<dataFrameNum;i++)
            frameEnergyFrequency[i]=frameLittleEnergy[i]*frameLittleZero[i];

        //得到有效帧数据
        int beginIndex=findBeginning(frameEnergyFrequency);
        int endIndex=findEnd(frameEnergyFrequency);
        effectiveFrameNum=endIndex-beginIndex+1;
        if(effectiveFrameNum>0)
            effectiveFrameDouble=new double[effectiveFrameNum][240];
        else
            effectiveFrameDouble=new double[1][240];
        for(int i=0;i<effectiveFrameNum;i++)
        {
            for(int j=0;j<240;j++)
                effectiveFrameDouble[i][j]=dataFrameDouble[beginIndex+i][j];
        }

        //得到有效语音数据
        effectiveDouble=frameToData(effectiveFrameDouble);
    }

    /**
     * Method littleEnergy
     *
     * 计算短时能量
     */
    private double[] littleEnergy()
    {
        double frameLittleEnergy[]=new double[dataFrameNum];
        for(int i=0;i<dataFrameNum;i++)
        {
            for(int j=0;j<240;j++)
                frameLittleEnergy[i]+=dataFrameDouble[i][j]*dataFrameDouble[i][j];
        }

        return frameLittleEnergy;
    }

    /**
     * Method signum
     *
     * 符号函数
     */
    public double signum(double x)
    {
        if(x>=0)
            return 1;
        else
            return -1;
    }

    /**
     * Method littleZero
     *
     * 计算短时过零率
     */
    private double[] littleZero()
    {
        double frameLittleZero[]=new double[dataFrameNum];
        for(int i=0;i<dataFrameNum;i++)
        {
            for(int j=1;j<240;j++)
                frameLittleZero[i]+=0.5*Math.abs(signum(dataFrameDouble[i][j])-signum(dataFrameDouble[i][j-1]));
        }

        return frameLittleZero;
    }

    /**
     * Method findBeginning
     *
     * 检测语音起始点
     */
    private int findBeginning(double[] efv)
    {
        int beginIndex=0;
//      Vector beginIndexVector=new Vector();
        double r=0.5;
//      System.out.println(dataFrameNum);
        for(int t=0;t<dataFrameNum-1;)
        {
            if(efv[t]!=0)
            {
                int j;
                for(j=0;j<dataFrameNum-t-1;j++)
                {
                    if(efv[t+j]>efv[t+j+1])
                    {
                        r=efv[t+j]/efv[t];
                        break;
                    }
                }
/*              System.out.print(t+":");
                System.out.print(r);
                System.out.print("   efv[t]:");
                System.out.println(efv[t]);*/
                if(r>30)
                {
//                  beginIndexVector.addElement(new Integer(t));
                    beginIndex=t;
                    break;
                }
                else
                    t=t+j+1;
            }
            else
                t++;
        }

/*      beginIndexVector.trimToSize();
        int indexNum=beginIndexVector.capacity();
        beginIndex=new int[indexNum];
        System.out.println("beginIndex:");
        for(int i=0;i<indexNum;i++)
        {
            Integer indexInteger=(Integer)beginIndexVector.get(i);
            beginIndex[i]=indexInteger.intValue();
            System.out.println(beginIndex[i]);
        }*/
//      System.out.println("beginIndex:"+beginIndex);

        return beginIndex;
    }

    /**
     * Method findEnd
     *
     * 检测语音终点
     */
    private int findEnd(double[] efv)
    {
        int endIndex=0;
//      Vector endIndexVector=new Vector();
        double r=0.5;
//      System.out.println(dataFrameNum);
        for(int t=dataFrameNum-1;t>0;)
        {
            if(efv[t]!=0)
            {
                int j;
                for(j=0;j<t-1;j++)
                {
                    if(efv[t-j]>efv[t-j-1])
                    {
                        r=efv[t-j]/efv[t];
                        break;
                    }
                }
/*              System.out.print(t+":");
                System.out.print(r);
                System.out.print("   efv[t]:");
                System.out.println(efv[t]);*/
                if(r>30)
                {
//                  endIndexVector.addElement(new Integer(t));
                    endIndex=t;
                    break;
                }
                else
                    t=t-j-1;
            }
            else
                t--;
        }

/*      endIndexVector.trimToSize();
        int indexNum=endIndexVector.capacity();
        endIndex=new int[indexNum];
        System.out.println("endIndex:");
        for(int i=0;i<indexNum;i++)
        {
            Integer indexInteger=(Integer)endIndexVector.get(i);
            endIndex[i]=indexInteger.intValue();
            System.out.println(endIndex[i]);
        }*/
//      System.out.println("endIndex:"+endIndex);

        return endIndex;
    }

    /**
     * Method frameToData
     *
     * 由帧数据还原语音数据
     */
    private double[] frameToData(double[][] frame)
    {
        double[] data;
        int frameNum=frame.length;
//      System.out.println(frameNum);

        if(frameNum!=0)
        {
            int dataLength=frameNum*120+120;
            data=new double[dataLength];
            for(int i=0;i<frameNum;i++)
            {
                for(int j=0;j<120;j++)
                    data[120*i+j]=frame[i][j];
            }
            for(int i=0;i<120;i++)
                data[frameNum*120+i]=frame[frameNum-1][120+i];
        }
        else
            data=new double[1];

        return data;
    }
}
